# Tieba API audit against aiotieba

This document records the read-only comparison between this project and `/Users/wanghao/Project/aiotieba` performed on 2026-04-28. The goal was to identify API contracts in TiebaLite-Next that may need updates based on aiotieba's latest Tieba API definitions.

## Reference baseline

- aiotieba app host: `tiebac.baidu.com`
- aiotieba versions:
  - latest/write-form baseline: `22.5.1.0`
  - stable/protobuf-read baseline: `12.64.1.1`
- TiebaLite-Next still contains multiple older app versions: `7.2.0.0`, `8.2.2`, `11.10.8.6`, `12.25.1.0`, `12.35.1.0`, `12.52.1.0`.

## Highest-priority findings

| Priority | Area | Finding | Suggested action |
| --- | --- | --- | --- |
| High | App host | App JSON/form clients still use `http://c.tieba.baidu.com/`, while aiotieba uses `tiebac.baidu.com`. | Migrate app/form clients to `https://tiebac.baidu.com/` after targeted smoke testing. |
| High | Cookie header | `getCookie()` joined pairs as `key:value` instead of standard `key=value`. | Fix immediately; this is a clear low-risk bug. |
| High | Client versions | Common app/protobuf versions are stale and scattered. | Centralize and update versions gradually, starting with protobuf read APIs. |
| High | Protobuf auth | V12/V12_POST `buildCommonRequest()` ignored explicit `bduss` and `stoken` arguments. | Use supplied auth values when present, falling back to `AccountUtil`. |
| High | Protobuf CommonRequest | aiotieba's `CommonReq` includes `idfv = 60`; TiebaLite's `CommonRequest.proto` does not. Field 44 is named differently. | Add `idfv = 60`; consider renaming field 44 later if generated-code churn is acceptable. |
| High | Protobuf multipart | aiotieba sends a minimal multipart with only `data`; TiebaLite may add outer fields/signature. | If protobuf calls are flaky, align multipart framing with aiotieba in a focused change. |

## JSON/form endpoints that are stale or should prefer protobuf

| Endpoint | Current TiebaLite use | aiotieba/latest direction |
| --- | --- | --- |
| `/c/f/pb/page` | JSON/form exists in `OfficialTiebaApi`; protobuf also exists. | Prefer protobuf cmd `302001`. |
| `/c/f/pb/floor` | JSON/form exists in `OfficialTiebaApi`; protobuf also exists. | Prefer protobuf cmd `302002`. |
| `/c/f/frs/page` | JSON/form exists in `MiniTiebaApi`; protobuf also exists. | Prefer protobuf cmd `301001`. |
| `/c/c/post/add` | JSON/form exists in `OfficialTiebaApi`; protobuf also exists. | Prefer protobuf cmd `309731`. |
| `/c/u/feed/replyme` | JSON/form exists in `NewTiebaApi`. | aiotieba uses protobuf cmd `303007`; TiebaLite lacks this protobuf API. |
| `/c/u/feed/userpost` | JSON/form exists in `MiniTiebaApi`; protobuf also exists. | Prefer protobuf cmd `303002`. |
| `/c/u/user/profile` | JSON/form exists in `OfficialTiebaApi`; protobuf also exists. | Prefer protobuf cmd `303012`. |

## Endpoint-specific differences to verify

### `/c/c/agree/opAgree`

aiotieba's object-type semantics:

- thread-level like: `obj_type = 3`
- post/floor like: `obj_type = 1`
- comment/subpost like: `obj_type = 2`
- dislike: `agree_type = 5`

TiebaLite has defaults that can conflict with these modes. Centralize object-type calculation before changing call sites.

### `/c/c/forum/msign`

TiebaLite uses the app endpoint with `forum_ids`, `tbs`, `authsid`, `stoken`, and `user_id`. aiotieba uses a web/hybrid flow on `https://tieba.baidu.com/c/c/forum/msign` with `subapp_type=hybrid`, `Subapp-Type: hybrid`, and web cookies.

This is a major contract difference. Treat migration as a feature-level change with manual testing.

### `/c/s/login`

TiebaLite sends `bdusstoken` as `BDUSS|null` plus older fields such as `stoken`, `user_id`, channel fields, and `authsid`. aiotieba sends raw `BDUSS` and `_client_version` only.

This can be simplified later, but login changes should be tested carefully.

### Protobuf add-post

aiotieba sets `from_fourm_id = fid` and uses `post_from = "3"`. TiebaLite supports `from_fourm_id` in proto but does not set it in the current add-post construction, and uses `post_from = "13"` for normal posts/replies.

Add `from_fourm_id` first if posting reliability is an issue; verify `post_from` separately.

### Protobuf FRS page

aiotieba sends raw `kw`, normalizes first page `pn` to `0`, and sets `rn_need = rn + 5`. TiebaLite URL-encodes `kw` inside protobuf, sends `pn = page`, and uses fixed `rn = 90`, `rn_need = 30`.

This should be verified with forum list pagination and sort modes before changing.

## Lower-risk cleanup candidates

- Simplify user follow/unfollow parameters to aiotieba's minimal set once host/version changes are stable.
- Simplify forum follow/unfollow parameters after testing.
- Simplify delete thread/post parameters after moderator-flow testing.
- Simplify `/c/s/sync` to aiotieba's minimal `BDUSS`, `_client_version`, and `cuid` shape after login/session tests.
- Revisit signing: aiotieba signs every app form request, while TiebaLite signs only under narrower conditions.

## Recommended implementation order

1. Fix malformed Cookie header construction.
2. Fix protobuf auth argument fallback for V12/V12_POST.
3. Add missing `idfv = 60` to `CommonRequest.proto` if generated-code changes are acceptable.
4. Update app host/version in small endpoint groups with smoke tests.
5. Migrate stale JSON/form endpoints to existing protobuf paths.
6. Add missing protobuf `replyme` cmd `303007` if message notifications require parity with aiotieba.
