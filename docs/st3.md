# st3 说明

本文档简述开发 st3 功能时应注意的事项。相关机制除非特殊注明，否则均为经验推导，未经过代码级验证，仅供参考。

## clearType “不可靠”

每次 `songId` + `songDifficulty` 得到最高分后，将其 `score`、`pure`、`far`、`lost` 等写入对应列。
对于 `clearType`，则会按 *PURE MEMORY* &gt; *FULL RECALL* &gt; *CLEAR* 的优先级顺序判断是否需要写入。

这就带来一个问题。假设某个难度首先打出了一个 (FAR 10 · LOST 0) 的 *FULL RECALL* 成绩，那么 st3 便会记录该成绩与 `clearType`。
假设以后再打出一个 (FAR 0 · LOST 1) 的成绩，明显该成绩分数更高，会覆盖原有成绩行。
但因为 1 个 LOST 导致其 `clearType` 为*普通 CLEAR*， 优先级小于 *FULL RECALL*，那么 st3 中的 `clearType` 并不会更新。
所以在后续提取时便会见到 LOST != 0 但 `clearType` 显示为 *FULL RECALL* 的诡异情况。

在 B50 计分规则更新前，st3 导入器引入了所谓“可靠性”检查：若 `clearType` 和实际成绩不匹配，则实际转为 `PlayResult` 时将其置空。
但 B50 引入了 *TRACK COMPLETE* +0.2 的计分机制，所以为确保计分准确性，移除了该项检查。

沉痛悼念：
```kotlin
// git show 853a633bc53701933fc112fece9d4037531fd70b:core/src/main/kotlin/xyz/sevive/arcaeaoffline/core/database/externals/importers/ArcaeaSt3PlayResultImporter.kt | sed -n '/val isClearTypeReliable/,+34p'

val isClearTypeReliable: Boolean
    get() {
        if (clearType == ArcaeaPlayResultClearType.FULL_RECALL.value && lost != 0) return false
        if (clearType == ArcaeaPlayResultClearType.PURE_MEMORY.value && lost != 0 && far != 0) return false

        return true
    }

fun toPlayResult(importDate: LocalDate): PlayResult {
    return PlayResult(
        // ...
        clearType =
            if (!isClearTypeReliable) null
            else clearType?.let { ArcaeaPlayResultClearType.fromInt(it) },
        // ...
    )
}
```

## 日期截断

> 截止 v7.0.255 可通过表现确认，在 v7.0.1c 代码中验证存在。

st3 理论上使用的是 Unix 秒级时间戳，实际打歌记录和备份上传的也是如此。

但在备份恢复时，游戏会将服务器回传的**秒级**时间戳视为**毫秒级**时间戳，除以 1000 后再存入 st3。
所以在当前 10 位时间戳的情况下，你将在新机上喜提 7 位时间戳，时间戳精度减少 1000s。

What's worse，在新机上传备份，7 位时间戳会覆盖服务器存档槽位并原样永久保存。
聪明的读者也猜到了，如果在新新机上恢复备份，那么将喜提 4 位时间戳，时间戳精度减少 1000000s，意味着精确时间不可能再恢复。

根据实测情况，该时间戳到最后可降为 0（你知道的，整数除法）。
导入器尽力在救了，但效果也视数据实际情况，一般并不理想。

> 为什么选 1489017600 为判断基准呢？因为那是 Arcaea 的发布日期。
>
> 你总不能在 Arcaea 开放下载前就产生分数记录了吧？

```kotlin
internal fun fixSt3Timestamp(ts: Long?): Long? {
    if (ts == null || ts > 1489017600) return ts

    val isFixable = (ts in 1489..9999) || ts > 14889
    if (!isFixable) return null

    return ts.toString().padEnd(10, '0').toLong()
}
```
