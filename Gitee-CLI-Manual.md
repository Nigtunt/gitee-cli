# Gitee CLI 使用说明书

`gitee-cli` 是一个基于命令行的 Gitee 交互工具，设计宗旨为“机器优先 (Machine-First)”，采用绝对非交互模式，所有的参数均可以通过命令行或环境变量传入，其输出支持严格的 JSON 格式。本工具的命令结构和功能设计很大程度上对标了 GitHub CLI (`gh`)。

## 全局参数选项

以下全局参数可用于任何命令和子命令中：

- `--debug`：启用调试模式，打印详细的 API 请求和响应数据。
- `--json`：强制所有的输出结果转换为合法的 JSON 格式。
- `-R, --repo=<repo>`：指定目标仓库，格式必须为 `<OWNER>/<REPO>`。
- `-h, --help`：打印命令的帮助信息并退出。
- `-V, --version`：打印版本信息并退出。

---

## 1. 认证相关命令 (`gitee auth`)

用于管理 Gitee 的用户认证状态和令牌（Token）。

### 1.1 登录 (`gitee auth login`)
**用途**：使用 Gitee 个人访问令牌（Personal Access Token）进行身份认证并保存。

**用法**：
```bash
gitee auth login -t=<token>
```
**选项**：
- `-t, --token=<token>`：【必填】Gitee 个人访问令牌。

### 1.2 登出 (`gitee auth logout`)
**用途**：移除并清理本地已保存的认证令牌信息。

**用法**：
```bash
gitee auth logout
```

### 1.3 状态查看 (`gitee auth status`)
**用途**：查看当前 CLI 的认证状态（确认是否已登录）。

**用法**：
```bash
gitee auth status
```

---

## 2. 配置相关命令 (`gitee config`)

用于管理 `gitee-cli` 的本地配置信息。支持的基础配置键包括：`base_url`（API 根地址）、`token`（认证令牌）、`remote_pattern` 等。

### 2.1 获取配置 (`gitee config get`)
**用途**：获取指定配置键的对应值。

**用法**：
```bash
gitee config get <key>
```
**参数**：
- `<key>`：配置项名称（如 `base_url`, `token`, `remote_pattern`）。

### 2.2 查看所有配置 (`gitee config list`)
**用途**：列出当前所有的配置项及对应的值。

**用法**：
```bash
gitee config list
```

### 2.3 设置配置 (`gitee config set`)
**用途**：设置指定配置键的值。

**用法**：
```bash
gitee config set <key> <value>
```
**参数**：
- `<key>`：配置项名称。
- `<value>`：需要设置的值。

---

## 3. Issue 相关命令 (`gitee issue`)

用于管理和操作仓库的 Issue 流水线。

### 3.1 创建 Issue (`gitee issue create`)
**用途**：在指定仓库中创建一个新的 Issue。

**用法**：
```bash
gitee issue create -t=<title> [选项]
```
**选项**：
- `-t, --title=<title>`：【必填】Issue 标题。
- `-b, --body, --description=<description>`：Issue 的正文/描述。
- `--assignee=<assignee>`：指定负责人的 Gitee ID。
- `--labels=<labels>`：逗号分隔的标签列表（如 `bug,enhancement`）。
- `--priority=<priority>`：优先级，可选值为：`unassigned`, `low`, `medium`, `high`, `urgent`。
- `--branch-name=<branchName>`：关联的分支名称。
- `--plan-started-at=<planStartedAt>`：计划开始时间。
- `--due-date=<dueDate>`：计划完成时间。
- `--iid=<iid>`：指定企业级 Issue 的 IID（传入 0 代表自增，>0 则明确设定）。

### 3.2 列出 Issue (`gitee issue list`)
**用途**：列出指定仓库（通过 `-R` 参数传递）中的 Issue 列表。支持多维度筛选和模糊搜索。

**用法**：
```bash
gitee issue list [选项]
```
**选项**：
- `--state=<state>`：根据状态过滤，可选值为：`created`（默认）, `processing`, `rejected`, `finished`, `all`。
- `--search=<search>`：使用关键字对 Issue 进行模糊搜索。
- `--sort=<sort>`：排序字段，可选：`created`（默认）, `updated`。
- `--direction=<direction>`：排序方向，可选：`asc`, `desc`（默认）。
- `--page=<page>`：页码（默认为 1）。
- `--per-page=<perPage>`：每页结果数（默认为 30）。

### 3.3 查看 Issue 详情 (`gitee issue view`)
**用途**：查看某个特定 Issue 的详细情况。

**用法**：
```bash
gitee issue view <number> [选项]
```
**参数**：
- `<number>`：Issue 编号（如 `I1234A`）。

**选项**：
- `-c, --comments`：不仅查看详情，同时加载和显示所有的评论对话。

### 3.4 评论 Issue (`gitee issue comment`)
**用途**：对某个 Issue 添加评论。

**用法**：
```bash
gitee issue comment <number> -b=<body> [选项]
```
**参数**：
- `<number>`：Issue 编号（如 `I1234A`）。

**选项**：
- `-b, --body=<body>`：提交的评论正文。
- `-e, --editor`：跳过提示，打开系统默认文本编辑器进行评论编辑（目前不完全支持）。
- `-w, --web`：在浏览器中打开 Web 界面进行评论（目前不完全支持）。

---

## 4. Pull Request (PR) 相关命令 (`gitee pr`)

用于全流程管理 Gitee 的 Pull Request，涵盖了查看、创建、评审、合并等操作。

### 4.1 创建 PR (`gitee pr create`)
**用途**：从源分支创建一条发往目标分支的 Pull Request请求。

**用法**：
```bash
gitee pr create -t=<title> -H=<head> -B=<base> [选项]
```
**选项**：
- `-t, --title=<title>`：【必填】PR 请求的标题。
- `-H, --head=<head>`：【必填】你的源分支名称（包含你想合并的提交）。
- `-B, --base=<base>`：【必填】你想合并到的目标分支名称。
- `-b, --body=<body>`：PR 请求的详细正文。
- `--remove-source-branch`：合并后自动删除源分支。
- `--primary_reviewer_ids=<ids>`：逗号分隔的主要审查人员 ID 列表（如 `2,admin`）。
- `--primary_reviewer_num=<num>`：要求最少的主要审查人员数量（默认为 1）。

### 4.2 列出 PR (`gitee pr list`)
**用途**：列出仓库中的 Pull Request 列表，支持状态、分支、搜索词筛选。

**用法**：
```bash
gitee pr list [选项]
```
**选项**：
- `--state=<state>`：状态过滤，可选：`opened`（默认）, `closed`, `merged`, `drafted`, `all`。
- `--search=<search>`：关键词模糊搜索。
- `--source_branch=<branch>`：按源分支名称过滤。
- `--target_branch=<branch>`：按目标分支名称过滤。
- `--sort=<sort>`：排序规则，可选：`desc`（默认）, `asc`。
- `--page=<page>`：页码（默认 1）。
- `--per-page=<perPage>`：每页条数（默认 10）。

### 4.3 查看 PR (`gitee pr view`)
**用途**：查看特定 PR 的详情概览。

**用法**：
```bash
gitee pr view <number>
```
**参数**：
- `<number>`：PR 请求的编号值。

### 4.4 查看 PR 差异代码 (`gitee pr diff`)
**用途**：查看该 PR 修改的代码 Diff 结果。这在结合 LLM Agent 工具进行自动 Code Review 时特别有用。

**用法**：
```bash
gitee pr diff <number>
```
**参数**：
- `<number>`：PR 编号。

### 4.5 编辑 PR (`gitee pr edit`)
**用途**：修改现有的 PR 详情信息或状态。

**用法**：
```bash
gitee pr edit <number> [选项]
```
**参数**：
- `<number>`：PR 编号。

**选项**：
- `-t, --title=<title>`：更新 PR 标题。
- `-b, --body=<body>`：更新 PR 正文。
- `--state-event=<stateEvent>`：触发状态事件，可选值：`closed`, `opened`, `drafted`（默认为 `opened`）。

### 4.6 关闭 PR (`gitee pr close`)
**用途**：直接拒绝或关闭尚未合并的 PR 请求。

**用法**：
```bash
gitee pr close <number>
```

### 4.7 重新开启 PR (`gitee pr reopen`)
**用途**：将已经处于关闭状态的 PR 重新打开。

**用法**：
```bash
gitee pr reopen <number>
```

### 4.8 评审 PR (`gitee pr review`)
**用途**：对某个 PR 进行代码审查操作并提交评价建议和结论。

**用法**：
```bash
gitee pr review <number> [选项]
```
**参数**：
- `<number>`：PR 编号。

**选项**：
- `-b, --body=<body>`：审查的评论内容。
- `--state=<state>`：审查结论，可选值：`commented`（默认，仅评论）, `approved`（通过）, `rejected`（拒绝）, `initialized`（初始化）。

### 4.9 合并 PR (`gitee pr merge`)
**用途**：执行合并 Pull Request 到目标分支的操作。

**用法**：
```bash
gitee pr merge <number> [选项]
```
**参数**：
- `<number>`：PR 编号。

**选项**：
- `--merge-type=<mergeType>`：合并类型，可选值：`merge`（默认，普通合并）, `squash`（压扁合并）, `fast_forward`（快进合并）, `rebase`（变基合并）。
- `--squash`：仅勾选此项的话，将使用 Squash 合并方式。
- `--merge-commit-message=<message>`：指定合并操作的 Commit Message。

### 4.10 评论 PR (`gitee pr comment`)
**用途**：向某个 PR 追加一条普通讨论评论。

**用法**：
```bash
gitee pr comment <number> -b=<body>
```
**参数**：
- `<number>`：PR 编号。
- `-b, --body=<body>`：【必填】评论的正文内容。

---

## 5. 仓库相关命令 (`gitee repo`)

用于操作和查看 Gitee 的代码仓库情况。

### 5.1 查看仓库信息 (`gitee repo view`)
**用途**：在终端查看目标仓库（通过 `-R` 传入）的概要信息以及 README 文件内容。

**用法**：
```bash
gitee repo view [-R=<repo>]
```

### 5.2 克隆仓库 (`gitee repo clone`)
**用途**：将远程的 Gitee 仓库克隆到本地文件系统。

**用法**：
```bash
gitee repo clone [-R=<repo>] [选项]
```
**选项**：
- `--ssh`：使用 SSH URL (`git@gitee.com:...`) 而不是 HTTPS 进行克隆。
- `--depth=<depth>`：指定 `--depth` 参数，执行浅克隆。

### 5.3 Fork 仓库 (`gitee repo fork`)
**用途**：将目标仓库 Fork 一份镜像到当前的经过认证登录的自己用户账户名下。

**用法**：
```bash
gitee repo fork -R=<repo>
```
