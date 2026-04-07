# 日常开发工作流程

## 场景 1：开发新功能

  1. 确保在 develop 分支，并同步最新代码
  git checkout develop
  git pull origin develop

  2. 从 develop 创建新的 feature 分支
  git checkout -b feature/your-feature-name

  3. 进行开发，随时提交代码
  git add .
  git commit -m "feat: 实现了某某功能"

  4. 继续开发，多次提交
  git add .
  git commit -m "feat: 完善某某功能的逻辑"

  5. 开发完成后，推送到远程
  git push origin feature/your-feature-name

  6. 在 GitHub 上创建 Pull Request，目标分支选择 develop
  等待团队成员 review 后合并

## 场景 2：同步 develop 分支的最新内容

  1. 在你的 feature 分支上
  git checkout feature/your-feature-name

  2. 拉取 develop 的最新代码并合并到当前分支
  git fetch origin develop
  git merge origin/develop
  或者使用 rebase（保持提交历史更清晰） rebase和merge二选一，一般建议在develop分支上用merge，feature及其子分支上用rebase
  git rebase origin/develop

  3. 如果有冲突，解决冲突后
  git add .
  git rebase --continue  如果用的是 rebase
  或
  git commit  # 如果用的是 merge

  4. 推送到远程（如果用了 rebase，需要强制推送）
  git push origin feature/your-feature-name --force-with-lease

## 场景 3：每天开始工作前

  1. 先同步 develop 分支
  git checkout develop
  git pull origin develop

  2. 切回你的 feature 分支
  git checkout feature/your-feature-name

  3. 将 develop 的更新合并到你的分支
  git merge develop
  或
  git rebase develop

## 场景 4：提交代码的最佳实践

  1. 查看修改了哪些文件
  git status

  2. 查看具体修改内容
  git diff

  3. 添加特定文件（推荐，避免误提交）
  git add src/main/java/com/rabbiter/oes/controller/ExamController.java

  4. 或添加所有修改
  git add .

  5. 提交，写清楚的提交信息
  git commit -m "feat: 添加考试管理的删除功能"

  6. 推送到远程
  git push origin feature/your-feature-name

  ---

## 提交信息规范建议

  1. 新功能
  git commit -m "feat: 添加学生成绩查询接口"

  2. 修复 bug
  git commit -m "fix: 修复登录时 Cookie 未正确设置的问题"

  3. 文档更新
  git commit -m "docs: 更新 README 中的部署说明"

  4. 代码重构
  git commit -m "refactor: 重构试卷管理的服务层逻辑"

  5. 性能优化
  git commit -m "perf: 优化题库查询的 SQL 性能"

  6. 测试相关
  git commit -m "test: 添加用户登录的单元测试"

  ---

## 常用 Git 命令速查

  查看当前分支
  git branch

  查看所有分支（包括远程）
  git branch -a

  切换分支
  git checkout branch-name

  创建并切换到新分支
  git checkout -b new-branch-name

  查看提交历史
  git log --oneline --graph

  查看远程仓库信息
  git remote -v

  拉取远程所有分支的最新信息
  git fetch origin

  删除本地分支
  git branch -d branch-name

  删除远程分支
  git push origin --delete branch-name

  暂存当前修改（临时切换分支时用）
  git stash

  恢复暂存的修改
  git stash pop

  撤销未提交的修改
  git checkout -- filename

  撤销已 add 但未 commit 的文件
  git reset HEAD filename

  ---
  团队协作注意事项

  1. 永远不要直接推送到 main 分支
  2. develop 分支也尽量通过 PR 合并，不要直接推送
  3. feature 分支命名要有意义：feature/exam-management、feature/student-score
  4. 经常同步 develop 分支，避免最后合并时冲突太多
  5. 提交前先 pull，确保本地是最新代码
  6. 小步提交，不要一次性提交几百行代码
  7. PR 描述要清楚，说明做了什么、为什么这么做

  ---

## 冲突解决流程

  当 merge 或 rebase 时出现冲突
  
  1. 查看冲突文件
  git status

  2. 打开冲突文件，手动解决冲突标记
  
  3. 解决后，标记为已解决
  git add conflicted-file.java

  4. 继续合并
  git merge --continue
  或
  git rebase --continue

  5. 推送
  git push origin feature/your-feature-name

  ---
