## Setting Up This on Your Local Machine

1. `cd` into your working directory.
2. Run `git clone https://github.com/melvillewong/SSH.git`. This will automatically create a new directory for the repository, named after the repository
3. `cd SSH` to get into the repository.
4. Run `code .` to start coding in VS Code.

## Why Do We Need Branches?

### Simplifies Collaboration

When collaborating with other developers, branches make it easy to:

-   **Submit changes for review**: Developers create pull requests (PRs) to propose merging their branch into the main branch. This allows for code review before changes are incorporated.
-   **Resolve conflicts**: If multiple developers change the same lines of code, Git can help merge changes (and flag conflicts) when merging branches.

### Track and Organize Workflow

Branches allow you to organize your development workflow. For instance, you can create specific branches for:

-   Feature development (`feature/login`)
-   Bug fixes (`bugfix/crash-on-load`)
-   Release preparation (`release/v1.2`)
-   Hotfixes (`hotfix/missing-logo`)

This helps maintain clarity and allows you to work on different types of tasks without mixing them up.

## Setting Up a Branch and Pull Request

1. Initially, there is a default branch named `main` in your local repository, but do not make changes directly on this branch.
2. `git checkout -b <feature/branch-name>` to create and switch into the feature branch.
3. `git branch` to check the list of branches and identify your current branch (marked with \*)
4. `git push origin <feature/branch-name>` to push your feature branch to remote repository (Github)
5. Go to your Github repository and submit a PR for approval to merge your feature branch with the main branch

## Make Sure Your Branches are on the Latest Version

1. `git checkout main` to switch to main branch.
2. `git fetch origin` to fetch the latest changes from the remote repository.
3. `git merge origin/main` to merge the remote main changes into your local main branch.
4. `git checkout <feature/branch-name>` to switch back to your feature branch.
5. `git merge main` to merge the main branch changes into your feature branch.
