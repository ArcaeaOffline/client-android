package xyz.sevive.arcaeaoffline.core.ocr

import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

@Suppress("FunctionName")
private fun Mat._mean(): Scalar {
    return Core.mean(this)
}

@Suppress("FunctionName")
private fun Mat._median(): Scalar {
    if (this.empty()) throw IllegalArgumentException("This matrix is empty")

    val numbersCount = this.rows() * this.cols()

    // convert mat to 1d array
    val arr = FloatArray(numbersCount)
    this.get(0, 0, arr)

    arr.sort()

    val median =
        if (numbersCount % 2 == 0) (arr[numbersCount / 2 - 1] + arr[numbersCount / 2]) / 2.0
        else arr[numbersCount / 2]

    return Scalar(median.toDouble())
}

fun Mat.toHashByteArray(): ByteArray {
    val size = this.size()
    val data = ByteArray((size.width * size.height).toInt())
    this.get(0, 0, data)
    return data
}

object ImageHashers {
    /**
     * Ensure all the hashes are using the same resizing algorithm.
     */
    private fun resizeImage(img: Mat, size: Size): Mat {
        val imgResized = Mat()
        Imgproc.resize(img, imgResized, size, 0.0, 0.0, Imgproc.INTER_AREA)
        return imgResized
    }

    private fun average(imgGray: Mat, hashSize: Double): Mat {
        val imgSize = Size(hashSize, hashSize)
        val imgResized = resizeImage(imgGray, imgSize)

        val hashMat = Mat()
        Core.compare(imgResized, imgResized._mean(), hashMat, Core.CMP_GT)
        return hashMat
    }

    /**
     * Computes a simple hash comparing the intensity of each pixel in
     * a resized version of the image to the mean.
     *
     * @see <a href="https://www.hackerfactor.com/blog/index.php?/archives/432-Looks-Like-It.html">Reference</a>
     * @see <a href="https://github.com/JohannesBuchner/imagehash">ImageHash (python library)</a>
     * @see <a href="https://github.com/thorn-oss/perception>perception (python library)</a>
     */
    fun average(imgGray: Mat, hashSize: Int): Mat {
        return average(imgGray, hashSize.toDouble())
    }

    private fun difference(imgGray: Mat, hashSize: Double): Mat {
        val imgSize = Size(hashSize + 1.0, hashSize)
        val imgResized = resizeImage(imgGray, imgSize)

        val previous = imgResized.submat(0, imgResized.rows(), 0, imgResized.cols() - 1)
        val current = imgResized.submat(0, imgResized.rows(), 1, imgResized.cols())

        val hashMat = Mat()
        Core.compare(previous, current, hashMat, Core.CMP_GT)
        return hashMat
    }

    /**
     * A hash based on the differences between adjacent pixels.
     *
     * @see <a href="https://www.hackerfactor.com/blog/index.php?/archives/529-Kind-of-Like-That.html">Reference</a>
     * @see <a href="https://github.com/JohannesBuchner/imagehash">ImageHash (python library)</a>
     * @see <a href="https://github.com/thorn-oss/perception>perception (python library)</a>
     */
    fun difference(imgGray: Mat, hashSize: Int): Mat {
        return difference(imgGray, hashSize.toDouble())
    }

    private fun dct(
        imgGray: Mat,
        hashSize: Double = 16.0,
        highFreqFactor: Double = 4.0
    ): Mat {
        val imgSizeBase = hashSize * highFreqFactor
        val imgSize = Size(imgSizeBase, imgSizeBase)

        val imgResized = resizeImage(imgGray, imgSize)
        imgResized.convertTo(imgResized, CvType.CV_32FC1)
        val dctMat = Mat()
        Core.dct(imgResized, dctMat)
        val hashMat = dctMat.submat(0, hashSize.toInt(), 0, hashSize.toInt()).clone()
        Core.compare(hashMat, hashMat._median(), hashMat, Core.CMP_GT)
        return hashMat
    }

    /**
     * The DCT hash, as known as pHash, a hash based on discrete cosine transforms of images.
     *
     * @see <a href="https://www.hackerfactor.com/blog/index.php?/archives/432-Looks-Like-It.html">Reference</a>
     * @see <a href="https://www.phash.org/docs/pubs/thesis_zauner.pdf">PDF Paper</a>
     * @see <a href="https://github.com/JohannesBuchner/imagehash">ImageHash (python library)</a>
     * @see <a href="https://github.com/thorn-oss/perception>perception (python library)</a>
     */
    fun dct(imgGray: Mat, hashSize: Int = 16, highFreqFactor: Int = 4): Mat {
        return dct(imgGray, hashSize.toDouble(), highFreqFactor.toDouble())
    }

    /**
     * Return the hamming distance between [hash1] and [hash2].
     */
    fun compare(hash1: Mat, hash2: Mat): Int {
        /**
         * We're supposed to use XOR(^), but for boolean values, the result are the
         * same using NEQ(!=). Since OpenCV only provides [org.opencv.core.Core.CMP_NE],
         * we'll use it instead.
         */
        assert(hash1.size().equals(hash2.size())) { "Input matrix size mismatch" }
        val cmpResult = Mat()
        Core.compare(hash1, hash2, cmpResult, Core.CMP_NE)
        return Core.countNonZero(cmpResult)
    }
}
