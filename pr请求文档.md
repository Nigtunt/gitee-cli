## Gitee Pull Request 核心接口参考 (已严格校对)

### 1. 查询项目的合并请求记录 (类似 `gh pr list`)

- **请求方式**: `GET`
    
- **请求路径**: `/projects/:id/merge_requests`
    
- **常用参数**:
    
    - `id` (number/string, **必填**): URI参数：仓库ID或仓库fullpath 。
        
    - `state` (string, 否): URL Query参数：pr状态（opened/closed/merged/drafted/reopened），不填默认为all 。
        
    - `page` (number, 否): URL Query参数：第几页；默认 1 。
        
    - `per_page` (number, 否): URL Query参数：每页条数；默认20 。
        
    - `search` (string, 否): URL Query参数：关键字搜索 。
        

### 2. 获取单个合并请求详情 (类似 `gh pr view`)

- **请求方式**: `GET`
    
- **请求路径**: `/projects/:id/merge_requests/:iid`
    
- **常用参数**:
    
    - `id` (string, **必填**): URI参数：仓库ID或仓库fullpath 。
        
    - `iid` (number, **必填**): URI参数：PullRequest的iid 。
        

### 3. 创建合并请求 (类似 `gh pr create`)

- **请求方式**: `POST`
    
- **请求路径**: `/projects/:id/merge_requests`
    
- **常用参数**:
    
    - `id` (number/string, **必填**): URI参数：仓库ID或仓库fullpath 。
        
    - `source_branch` (string, **必填**): 源分支 。
        
    - `target_branch` (string, **必填**): 目标分支 。
        
    - `title` (string, **必填**): pr标题 。
        
    - `description` (string, 否): pr 描述 。
        
    - `remove_source_branch` (boolean, 否): 是否删除源分支 。
        

### 4. 更新/编辑合并请求 (类似 `gh pr edit`)

_⚠️ 修正：`state_event` 在原文档中是必填参数，上次遗漏了该限制。_

- **请求方式**: `PUT`
    
- **请求路径**: `/projects/:id/merge_requests/:iid`
    
- **常用参数**:
    
    - `id` (number/string, **必填**): URI参数：仓库ID或仓库fullpath 。
        
    - `iid` (number, **必填**): URI参数：PullRequest的iid 。
        
    - `state_event` (string, **必填**): Body参数：状态 (closed/opened/drafted) 。
        
    - `title` (string, 否): Body参数：MR 标题 。
        
    - `description` (string, 否): Body参数：描述 。
        

### 5. 关闭 / 重新打开合并请求 (类似 `gh pr close` / `gh pr reopen`)

_⚠️ 注意：关闭 PR 用的是 `POST` 而不是 `DELETE` 或 `PUT`。_

- **关闭 PR**:
    
    - 请求方式/路径: `POST /projects/:id/merge_requests/:iid`
        
    - 必传参数: `id`, `iid` 。
        
- **重新打开 PR**:
    
    - 请求方式/路径: `POST /projects/:id/merge_requests/:iid/reopen`
        
    - 必传参数: `id`, `iid` 。
        

### 6. 通过合并请求 (类似 `gh pr merge`)

- **请求方式**: `PUT`
    
- **请求路径**: `/projects/:id/merge_requests/:iid/merge`
    
- **常用参数**:
    
    - `id` (number/string, **必填**): URI参数：仓库ID或仓库fullpath 。
        
    - `iid` (number, **必填**): URI参数：PullRequest的iid 。
        
    - `merge_type` (string, 否): 合并方式：`merge`, `squash`, `fast_forward`, `rebase`；默认为 `merge` 。
        
    - `merge_commit_message` (string, 否): 合并的提交信息 。
        
    - `squash` (boolean, 否): 如果为true，则合并时提交将被压缩为单个提交 。
        

### 7. 获取变更文件及内容 (类似 `gh pr diff`)

_⚠️ 补充：原文档其实提供了两个 Diff 接口，一个是获取全部变更，一个是针对指定路径获取详情。_

- **方式一（获取全部变更内容）**:
    
    - 请求方式/路径: `GET /projects/:id/merge_requests/:iid/diffs`
        
    - 说明: 支持 `with_page` 分页，默认不分页返回所有 。
        
- **方式二（获取变更文件详情，支持指定文件）**:
    
    - 请求方式/路径: `POST /projects/:id/merge_requests/:iid/diffs`
        
    - 常用参数: `paths` (array of string, 否) 用于指定变更文件路径 ，`view_mode` (string, 否) 用于指定双栏或单栏展示 。
        

### 8. 评审与添加评论 (类似 `gh pr review`)

_⚠️ 修正：原文档中这几个接口路径缺少开头斜杠（如 `projects/:id...`），实际调用时记得拼上 `/`。同时补全了上次漏掉的必填字段。_

- **提交评审结论 (Approve/Reject 等)**:
    
    - 请求方式/路径: `POST projects/:id/merge_requests/:iid/reviews`
        
    - 必填参数: `id`, `iid`, `state` (commented/approved/rejected/initialized) 。
        
    - 条件必填: 当 `state` 是单纯评论时，`comment` 字段必填 。
        
- **添加普通/代码行评论**:
    
    - 请求方式/路径: `POST projects/:id/merge_requests/:iid/notes`
        
    - 必填参数: `id`, `iid`, `note` (评论内容), `noteable_type` (必须传 "PullRequest" 或 "PullRequestTheme") 。