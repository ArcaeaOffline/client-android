# 游玩结算机制

> 最后校对于 2026-11-06

本文档记录游玩结算各项数据机制，如分数、判定计数（PURE、FAR、LOST）、最大连击（MAX RECALL）、通关类型（Clear Type）等。

## 来源

* [机制](https://wiki.arcaea.cn/index.php?title=%E6%9C%BA%E5%88%B6&oldid=93758)
* [界面](https://wiki.arcaea.cn/index.php?title=%E7%95%8C%E9%9D%A2&oldid=93764)

已对原文改写与归纳，本文遵循原文 [CC BY-NC-SA 3.0](https://creativecommons.org/licenses/by-nc-sa/3.0/) 授权。

## 标记说明

* `[wiki]` Wiki 原文陈述
* `[infer]` 基于 Wiki 原文与实际游玩经验的推断行为
* `[unk]` 未知且 Wiki 未记载

## 范围

本文档重点关注常规游玩结算，即成绩会被游戏保存，并作为潜力值计算输入的游玩结果。

以下情形与常规规则存在出入：

* `[wiki]` 异象与特殊挑战（Tempestissimo 异象、Sigil of the Abyss 等），即使达成 PM/FR 条件也只显示 TRACK COMPLETE，且成绩不被游戏保存。
* `[wiki]` 2023 愚人节活动的 bug，曾出现判定全为 PURE 却未判为 PM 的情形。

`[infer]` 异象、特殊挑战、愚人节等成绩均不会作为潜力值计算输入，故以下不考虑这些特殊情况。

## 判定与计分

* `[wiki]` 判定分为 PURE、FAR、LOST 三种，PURE 按精确度再分大 PURE 与小 PURE。
* `[wiki]` 单个音符的基本分为 `10000000 ÷ 物量`。PURE 得全额，FAR 得一半，LOST 不得分。
* `[wiki]` 每个大 PURE 额外加 1 分，其余判定不产生附加分。
* `[wiki]` 显示分数向下取整。基本分与附加分全部拿满时为理论值，此时成绩为 `10000000 + 物量`。
* `[infer]` PM 成绩的分数范围是 [10000000, 10000000 + 物量]。
    * 前提：PM 的判定标准是无 FAR/LOST 判定而非分数超过 10000000。

| 判定    | 区间                                      |
|---------|-------------------------------------------|
| 大 PURE | ±25ms                                     |
| 小 PURE | ±25~50ms                                  |
| FAR     | ±50~100ms                                 |
| LOST    | ±100ms内未击中 **或** 提前 100~120ms 击中 |

## 物量

* `[wiki*]` 物量不按物件数计数，而按判定点计数。
* `[wiki*]` 普通 Tap、Arctap 计 1 物量，长条按拍数产生多个判定点，个别特殊物件不计入物量。
* `[infer]` 打完全程时 PURE + FAR + LOST 即等于物量。
* `[infer]` 中断游玩后，剩余未判定音符不计入判定计数。

## 连击

* `[wiki]` MAX RECALL 为本次游玩的最高连击数。
    * [界面](https://wiki.arcaea.cn/index.php?title=%E7%95%8C%E9%9D%A2&oldid=93764)
* `[wiki]` PURE 与 FAR 使连击数 +1，LOST 使连击数清空。
* `[infer]` `MAX RECALL ≤ 物量 − LOST`。
* `[infer]` FR 与 PM 成绩的 MAX RECALL 等于物量。

## 回忆收集条

`[wiki]` 每个谱面有固定的回忆系数，与物量和难度相关，谱面等级 ≥ 11 时额外 ×0.8。常规回忆条如下。

| 回忆条 | 初始回忆率 | PURE    | FAR       | LOST     | 通关条件    | 中断                    |
|--------|------------|---------|-----------|----------|-------------|-------------------------|
| Normal | 0          | +1×系数 | +0.5×系数 | −2       | 结算时 ≥ 70 | 无                      |
| Easy   | 0          | +1×系数 | +0.5×系数 | −1.2     | 结算时 ≥ 70 | 无                      |
| Hard   | 100        | +1×系数 | +0.5×系数 | 分段扣除 | 结算时 > 0  | 回忆率低于 0 时立即中断 |

* `[wiki]` Hard 的 LOST 按当前回忆率分段扣除（默认 −9×扣除倍率，回忆率 ≤ 30 时 −5×扣除倍率），倍率受搭档技能与陷落章法则影响。
* `[wiki]` 结算时未满足通关条件的，一律得到 TRACK LOST 评价。
* `[wiki]` 中断机制仅见于 Hard 及其变体条。Normal 与 Easy 回忆条没有中途中断机制，是否 TL 在结算时判定。
* `[wiki]` 搭档技能条与失落章 Beyond Challenge 存在增减规则不同的变体，与判定计数相关的要点：
    * 部分变体条的 FAR 也扣回忆率（Beyond 挑战等），这些变体条在成绩记录中仍归入 Normal/Easy/Hard 之一。
    * 部分搭档的机制使得全连也可 TRACK LOST。
    * 部分搭档的机制使得全 LOST 也可 TRACK COMPLETE。
* `[infer]` LOST = 0 不蕴含通关，全连 TRACK LOST 的成绩是合法存在。
* `[infer]` LOST 等于物量（全部 LOST）不蕴含 TRACK LOST：全部 LOST 通关的成绩是合法存在。

## 通关类型

`[wiki]`

* PURE MEMORY：所有判定均为 PURE，不区分大小 PURE。
    * 原文特注：PM 的判定标准是无 FAR/LOST 判定，而非分数超过 10000000。
* FULL RECALL：有 FAR 判定但无 LOST 判定，并且满足通关条件。
* Clear：达成通关条件但未达 PM/FR。
    * 通关标志按回忆条类型给出 Normal Clear、Easy Clear、Hard Clear。
    * 以 Easy Clear 通关的曲目不计入通关曲目数量。
* TRACK LOST：结算时未满足通关条件。

`[wiki]` 通关类型按优先级 PM > FR > Clear > TL，高类型覆写低类型。例如，PM 能覆写 FR、Clear 与 TL，FR 能覆写 Clear 与 TL，TL 不能覆写任何类型。

`[infer]` 由定义与覆写规则：

* PM ⇒ FAR = 0 且 LOST = 0。
* FR ⇒ LOST = 0 且 FAR ≥ 1。
* FAR = 0 且 LOST = 0（全部判定为 PURE）⇒ PM。
    * 全 PURE 成绩覆写包括 TL 在内的其他评价。
* Clear 徽标（Normal/Easy/Hard）⇒ LOST ≥ 1。
* FR/PM ⇒ MAX RECALL 等于物量（见[连击](#连击)节）。
* 使用困难回忆条且被中断的 TL 成绩可能未打完全程，PURE + FAR + LOST 可小于物量。
* Normal/Easy 回忆条的 TL 打完全程，PURE + FAR + LOST 等于物量。
