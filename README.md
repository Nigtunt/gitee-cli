# Gitee CLI

`gitee-cli` 是一个基于命令行的 Gitee 交互工具，设计宗旨为"机器优先 (Machine-First)"，采用绝对非交互模式，所有的参数均可以通过命令行或环境变量传入，其输出支持严格的 JSON 格式。本工具的命令结构和功能设计很大程度上对标了 GitHub CLI (`gh`)。

## 项目特点

- 基于Java 21开发
- 使用Picocli作为命令行框架
- 支持JSON格式输出
- 支持原生镜像打包（通过GraalVM）

## 安装说明

### 重要提示

**当前项目必须通过GraalVM打包才能生成可执行文件**。普通的Maven打包（`mvn package`）只会生成JAR文件，无法直接运行。请按照以下步骤使用GraalVM进行原生镜像打包。

### 安装步骤

#### 1. 安装GraalVM JDK 21

首先需要安装GraalVM JDK 21，这是构建原生镜像的必要条件。

**Windows安装：**
```bash
# 下载GraalVM JDK 21 for Windows
# 从 https://www.graalvm.org/downloads/ 下载适用于Windows的GraalVM JDK 21

# 解压到指定目录，例如 C:\graalvm
# 添加到系统环境变量 PATH 中
```

**Linux/Mac安装：**
```bash
# 下载GraalVM JDK 21
wget https://github.com/graalvm/graalvm-ce-builds/releases/download/jdk-21.0.2/graalvm-jdk-21.0.2-linux-amd64.tar.gz

# 解压
tar -xzf graalvm-jdk-21.0.2-linux-amd64.tar.gz -C /usr/local/

# 添加到PATH
export PATH="/usr/local/graalvm-jdk-21.0.2/bin:$PATH"
```

#### 2. 验证GraalVM安装

```bash
java -version
# 应该显示类似 "GraalVM 21.0.2" 的输出
```

#### 3. 克隆项目

```bash
git clone https://gitee.com/your-repo/gitee-cli.git
cd gitee-cli
```

#### 4. 构建原生镜像

使用Maven的native profile进行构建：

```bash
# 清理之前的构建
mvn clean

# 使用native profile构建原生镜像
mvn -Pnative package
```

这个过程会：
1. 编译Java代码
2. 生成反射配置
3. 创建原生可执行文件

#### 5. 查找可执行文件

构建完成后，可执行文件会生成在`target/`目录下：

- Linux/Mac: `target/gitee`
- Windows: `target/gitee.exe`

#### 6. 运行CLI

```bash
# Linux/Mac
./target/gitee --version

# Windows
.\target\gitee.exe --version
```

### 快速使用

```bash
# 登录
gitee auth login -t=your_gitee_token

# 查看帮助
gitee --help

# 查看仓库信息
gitee repo view -R=owner/repo

# 创建Issue
gitee issue create -R=owner/repo -t="Bug report" -b="Description here"
```

## 构建选项

### 开发模式构建

如果只需要JAR文件用于开发测试：

```bash
mvn package
```

这将生成 `target/gitee-cli-0.1.0-SNAPSHOT.jar`，可以通过以下方式运行：

```bash
java -jar target/gitee-cli-0.1.0-SNAPSHOT.jar --version
```

### 原生镜像构建参数

可以通过修改`pom.xml`中的`native` profile来调整构建参数：

```xml
<buildArgs>
    <buildArg>--no-fallback</buildArg>          <!-- 禁用AOT回退 -->
    <buildArg>--enable-https</buildArg>        <!-- 启用HTTPS支持 -->
    <buildArg>-H:+ReportExceptionStackTraces</buildArg> <!-- 报告异常堆栈 -->
    <buildArg>-H:+AddAllCharsets</buildArg>    <!-- 添加所有字符集 -->
    <buildArg>-J-Dfile.encoding=UTF-8</buildArg> <!-- 设置文件编码 -->
    <buildArg>-H:PageSize=65536</buildArg>     <!-- 设置页面大小 -->
</buildArgs>
```

## 常见问题

### 1. 构建失败：找不到GraalVM

确保GraalVM JDK 21已正确安装并添加到PATH环境变量中。

### 2. 编译错误

确保使用的是Java 21，并且GraalVM版本与Java版本匹配。

### 3. 生成的可执行文件无法运行

检查文件权限（Linux/Mac）或确保使用正确的执行方式（Windows）。

## 贡献

欢迎提交Issue和Pull Request来改进这个项目。

## 许可证

本项目采用MIT许可证。 see `LICENSE` 文件获取详细信息。

