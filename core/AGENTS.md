# core

The device-side OCR engine: ONNX Runtime inference, OpenCV image operations, and the image-hash chart database. No Compose and no Android UI
code.

Kotlin sources live under `src/main/kotlin/`.

## Layout

```
xyz.sevive.arcaeaoffline.core/
├── ocr/
│   ├── ImageHashers, ImageHashesDatabase(+Builder), ImageHashItem
│   ├── device/        DeviceOcr, ONNX helper, ScreenshotDetect, performance benchmark
│   │   └── rois/      ROI definition / extractor / masker
│   └── opencv/        OpenCV helpers
└── di/CoreModule.kt
```

## Dependencies

ONNX Runtime and OpenCV resolve from `maven-local/` when it exists, otherwise from Maven Central. See root README for setup.
