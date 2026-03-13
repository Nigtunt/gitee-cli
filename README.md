# gitee-cli

## Overview
The `gitee-cli` project is a command-line tool developed in Java that allows users to interact with Gitee's API seamlessly. This tool provides various features for performing repository management tasks, including but not limited to the following:

- Create, update, and delete repositories
- Manage issues and pull requests
- Collaborate with team members through project boards

## Features
- **Simple Command Structure:** A user-friendly command structure that allows quick and efficient interactions with Gitee.
- **Authentication Support:** Secure authentication through tokens for Gitee API access.
- **Cross-Platform Compatibility:**Works smoothly on various operating systems (Windows, macOS, Linux).

## Installation
To install the `gitee-cli`, you can clone the repository and build the project using Maven:

```bash
# Clone the repository
git clone https://gitee.com/Nigtunt/gitee-cli.git

# Navigate into the directory
cd gitee-cli

# Build the project
mvn clean install
```

## Usage
Once installed, you can start using the tool with the following command structure:

```bash
gitee-cli [command] [options]
```

### Example Commands

- **Create a Repository:**
  ```bash
  gitee-cli create-repo --name <repo-name> --private
  ```

- **List Repositories:**
  ```bash
  gitee-cli list-repos
  ```

## Contributing
Contributions are welcome! Please feel free to submit a pull request or open an issue to discuss changes.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author
Developed by Nigtunt.