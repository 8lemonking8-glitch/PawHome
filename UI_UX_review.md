# PawHome 功能完整性审查报告

> 审查日期：2026-05-22（第 2 版）  
> 审查重点：功能完整性 — 每个功能从 UI → 逻辑 → 数据 → 反馈的端到端闭环  
> 审查范围：35 个 Java 文件、20 个布局 XML、8 个资源文件

---

## 一、总体结论

PawHome 已实现一个宠物领养应用的核心骨架：双角色系统、宠物浏览/搜索、收藏、领养申请、管理员审批。**UI 层的页面和组件较完整，但存在 1 个功能残缺（签名）、1 个功能缺失（领养历史）、1 个死代码文件，以及若干中等影响的数据流问题。** 整体处于"能跑通 happy path，但异常路径和完整闭环不足"的状态。

---

## 二、功能逐项检查

### 2.1 登录/注册 ✅ 基本完整

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 用户名密码输入 | ✅ | 含可见性切换 icon |
| 前端校验（空值） | ✅ | `LoginFragment.attemptLogin()` |
| 数据库校验 | ✅ | `UserRepository.login()` → `UserDao.login()` |
| 登录成功 → 跳转 | ✅ | `SessionManager.createSession()` → `AuthActivity.navigateToMain()` |
| 登录失败 → 错误提示 | ✅ | 红色错误文字 + 抖动动画 |
| 注册用户名查重 | ✅ | `UserDao.isUsernameExists()` |
| 注册成功 → 自动登录 | ✅ | 注册后直接跳转主页面 |
| 注册失败 → 错误提示 | ✅ | `tvError` 显示错误信息 |
| Session 持久化（重启保持登录） | ✅ | SharedPreferences |
| 角色路由（User/Admin） | ✅ | `SessionManager.isAdmin()` |
| **忘记密码** | ❌ | `tvForgotPassword` 有 UI 无功能 |
| **记住我** | ❌ | `cbRememberMe` 复选框被忽略 |

---

### 2.2 首页宠物浏览 ✅ 基本完整

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 宠物列表展示 | ✅ | StaggeredGrid 2 列瀑布流 |
| 分类筛选 (All/Dogs/Cats/Birds) | ✅ | ChipGroup 单选，切换后刷新 LiveData |
| 搜索（含 300ms debounce） | ✅ | 按名称和品种模糊搜索 |
| 搜索清除按钮 | ✅ | 清空输入恢复分类视图 |
| 图片加载 | ✅ | Drawable 资源直接加载 |
| 无图占位 | ✅ | 按类型显示不同颜色背景 |
| DiffUtil 增量更新 | ✅ | 列表刷新不闪烁 |
| **加载状态** | ✅ | ProgressBar 居中显示 |
| **空状态** | ✅ | 图标 + "No pets found" |
| **错误状态** | ❌ | 数据库异常时无降级 UI |
| **下拉刷新** | ❌ | 无 SwipeRefreshLayout |
| **搜索观察者泄漏** | ⚠️ | 每次搜索 attach 新 LiveData observer，旧 observer 未 remove（HomeFragment:125-129） |

---

### 2.3 宠物详情页 ✅ 基本完整

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 折叠式头部（视差图片） | ✅ | CollapsingToolbarLayout |
| 宠物信息展示 | ✅ | 名称、品种、年龄、性别、体型 |
| 领养故事 | ✅ | 描述文字 |
| 返回按钮 | ✅ | Toolbar navigation icon |
| 收藏切换 | ✅ | 心形按钮 + 弹跳动画 |
| 共享元素转场动画 | ✅ | 从卡片图片过渡到详情头图 |
| 非 AVAILABLE 宠物按钮禁用 | ✅ | FAB 变灰并显示状态文字 |
| 图片轮播（多图） | ❌ | `imageResIds` 字段存在但仅展示第一张图 |
| **加载状态** | ❌ | `loadPetDetails()` 无 loading 指示器 |
| **错误状态** | ❌ | pet 为 null 时无提示 |

---

### 2.4 收藏夹 ✅ 完整

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 收藏列表展示 | ✅ | 复用 PetAdapter + StaggeredGrid |
| 收藏切换 | ✅ | FavoriteManager (SharedPreferences per-user) |
| 滑动删除 | ✅ | ItemTouchHelper + Snackbar UNDO |
| 空状态 | ✅ | 图标 + "No favorites yet" |
| 加载状态 | ✅ | ProgressBar |
| 跨页面同步 | ✅ | onResume 刷新；详情页和首页收藏互相同步 |
| **错误状态** | ❌ | 加载失败无提示 |

---

### 2.5 领养申请流程 🔴 签名功能残缺

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 领养协议展示 | ✅ | 可滚动文本区域 |
| 签名板绘制 | ✅ | `SignatureView` 贝塞尔曲线触摸绘制 |
| 签名清除 | ✅ | `signatureView.clear()` |
| 未签名提交拦截 | ✅ | `Snackbar` 提示 "Please sign" |
| 重复申请拦截 | ✅ | `AdoptionRequestDao.getPendingRequestForPet()` |
| 提交成功提示 | ✅ | Snackbar + dismiss |
| 提交失败提示 | ✅ | Snackbar "duplicate request" |
| **签名图片持久化** | ❌ | **核心逻辑缺失**。代码保存的是占位字符串 `"signature_captured_timestamp_"`，而非 `SignatureView` 的 Bitmap。`getSignatureBitmap()` 方法被空置。用户画的签名字迹永远不会被保存。 |

**代码位置**：`AdoptionBottomSheet.java:67-68`

---

### 2.6 用户资料页 ⚠️ 领养历史功能缺失

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 用户信息展示 | ✅ | 头像、昵称、@用户名 |
| 联系方式展示 | ✅ | 邮箱、电话（或 "Not provided"） |
| 编辑资料入口 | ✅ | 编辑图标按钮 |
| 退出登录 | ✅ | 清除 Session + 跳转 AuthActivity |
| **领养历史列表** | ❌ | **功能未实现**。布局中已添加 `rvAdoptionHistory` RecyclerView 和 `item_adoption_history.xml` 卡片布局，但 `ProfileFragment.java` 中没有任何代码设置 Adapter、观察数据、切换空状态。`cardEmptyHistory` 始终可见。 |

---

### 2.7 编辑资料 ✅ 完整

| 检查项 | 状态 | 说明 |
|--------|------|------|
| BottomSheet 弹出 | ✅ | 从 ProfileFragment 触发 |
| 预填当前数据 | ✅ | `loadCurrentUserData()` → LiveData |
| 昵称/邮箱/电话输入 | ✅ | 三个 TextInputLayout |
| 保存到数据库 | ✅ | `UserRepository.update()` |
| Session 同步更新 | ✅ | `sessionManager.createSession()` |
| 成功后自动刷新 Profile | ✅ | `onProfileUpdated` 回调 |
| **邮箱格式验证** | ✅ | 已添加 `Patterns.EMAIL_ADDRESS` 验证 |
| **手机号格式验证** | ✅ | 已添加正则验证 |

---

### 2.8 管理员仪表盘 ⚠️ 功能正确但单薄

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 统计数字展示 | ✅ | 4 个卡片：总宠物数、可领养数、待处理请求、已领养数 |
| LiveData 实时更新 | ✅ | 来自 PetRepository 和 AdoptionRepository |
| 退出登录 | ✅ | AdminDashboardFragment |
| **加载状态** | ❌ | 无 ProgressBar，初始显示 "-" |
| **卡片点击跳转** | ❌ | 卡片无点击事件，不能跳转到对应列表 |
| **错误状态** | ❌ | 无 |

---

### 2.9 管理员宠物管理 ✅ 基本完整

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 宠物列表 | ✅ | LinearLayout RecyclerView |
| 添加宠物（表单） | ✅ | AddPetBottomSheet：名称、品种、类型、性别、年龄、体型、描述 |
| 图片选择（相册） | ✅ | `ActivityResultContracts.GetContent` |
| 拍照 | ✅ | `ActivityResultContracts.TakePicturePreview` |
| 删除宠物 | ✅ | 滑动 + AlertDialog 确认 |
| **重复项检查** | ❌ | 添加时不做同名检查 |
| **年龄格式验证** | ❌ | "abc" 输入会变成 "abc Years" 存入数据库 |
| **registerForActivityResult 时机** | ⚠️ | 字段级声明，在 Fragment attach 之前（Android 规范要求 `onCreate` 之后） |
| **错误状态** | ❌ | 无 |

---

### 2.10 管理员请求管理 ✅ 基本完整

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 请求列表展示 | ✅ | 含用户名、联系方式、宠物名、状态标签 |
| Tab 切换（Pending / All） | ✅ | TabLayout |
| 审批通过 | ✅ | 更新请求状态 + 自动标记宠物为 ADOPTED |
| 拒绝 | ✅ | 更新请求状态为 REJECTED |
| Action 按钮显隐 | ✅ | 仅 PENDING 状态显示 Approve/Reject |
| 审批后列表刷新 | ✅ | LiveData 自动更新 |
| **审批确认弹窗** | ❌ | 直接执行，无 AlertDialog 确认 |
| **同宠物其他请求自动拒绝** | ❌ | 审批通过后，该宠物其他 PENDING 不自动拒绝 |
| **审批 + 更新宠物状态无事务** | ❌ | 两步操作不在同一 Room 事务中 |
| **LiveData 观察者切换逻辑** | ⚠️ | `loadPendingRequests` 先移除错误 LiveData 的 observer（见 AdminRequestsFragment:87） |
| **错误状态** | ❌ | 无 |

---

### 2.11 暗色模式 ✅ 已适配

| 检查项 | 状态 | 说明 |
|--------|------|------|
| values-night/colors.xml | ✅ | 所有 surface/text/card 颜色有 dark 版本 |
| values-night/themes.xml | ✅ | Primary/secondary container 反转正确 |
| Admin 卡片颜色 dark 版本 | ✅ | 已添加暗色 admin card 颜色 |

---

### 2.12 死代码

| 文件 | 说明 |
|------|------|
| `MainActivity.java` | 模板 "Hello World" Activity，不在任何导航流中 |
| `activity_main.xml` | 配套布局，同样未使用 |
| AndroidManifest 中 `MainActivity` 注册 | 需同步移除 |

---

## 三、数据流端到端检查

| 流程 | UI | → Repo | → DAO | → DB | → 回 UI | 闭环 |
|------|-----|--------|-------|------|---------|------|
| 登录 | LoginFragment | UserRepository.login() | UserDao.login() | users 表 | SessionManager + navigate | ✅ |
| 注册 | RegisterFragment | UserRepository.register() | UserDao.insert() | users 表 | SessionManager + navigate | ✅ |
| 浏览宠物 | HomeFragment | PetRepository.getAvailablePets() | PetDao | pets 表 | LiveData → adapter | ✅ |
| 搜索宠物 | HomeFragment | PetRepository.searchPets() | PetDao | pets 表 | LiveData → adapter | ⚠️ observer 泄漏 |
| 宠物详情 | PetDetailActivity | PetRepository.getPetById() | PetDao | pets 表 | LiveData → bind | ✅ |
| 收藏切换 | PetAdapter/Fragment | FavoriteManager | — | SharedPreferences | 本地状态更新 | ✅ |
| 领养申请 | AdoptionBottomSheet | AdoptionRepository.createRequest() | AdoptionRequestDao.insert() | adoption_requests 表 | Snackbar | ❌ 签名未保存 |
| 编辑资料 | EditProfileBottomSheet | UserRepository.update() | UserDao.update() | users 表 | SessionManager + callback | ✅ |
| **领养历史** | **ProfileFragment** | **未调用** | **未调用** | **未查询** | **无** | ❌ **未实现** |
| 管理员统计 | AdminDashboardFragment | PetRepository + AdoptionRepository | PetDao + AdoptionRequestDao | pets + adoption_requests | LiveData → TextViews | ✅ |
| 添加宠物 | AddPetBottomSheet | PetRepository.insert() | PetDao.insert() | pets 表 | dismiss | ✅ |
| 删除宠物 | AdminPetsFragment | PetRepository.delete() | PetDao.delete() | pets 表 | LiveData 自动刷新 | ✅ |
| 审批请求 | AdminRequestsFragment | AdoptionRepository.approveRequest() | AdoptionRequestDao.update() | adoption_requests + pets | LiveData 自动刷新 | ⚠️ 无事务保护 |

---

## 四、问题汇总（按严重程度）

### 🔴 功能残缺/缺失（必须修复）

| # | 问题 | 位置 | 影响 |
|---|------|------|------|
| 1 | **签名图片从未保存** | `AdoptionBottomSheet.java:67-68` | 用户画的签名被丢弃，只存了占位字符串。领养协议的法律效力形同虚设。 |
| 2 | **领养历史功能未实现** | `ProfileFragment.java` | 布局和 item layout 已就绪，但缺少全部 Java 逻辑（Adapter、数据观察、状态切换）。用户看不到自己的领养记录。 |
| 3 | **忘记密码是死链接** | `fragment_login.xml` + `LoginFragment.java` | 按钮存在但无点击事件，违反用户预期。 |

### 🟡 数据一致性和健壮性

| # | 问题 | 位置 | 影响 |
|---|------|------|------|
| 4 | **审批请求无事务保护** | `AdoptionRepository.approveRequest()` | 请求状态和宠物状态可能不一致：请求 APPROVED 但宠物仍是 AVAILABLE |
| 5 | **审批后其他 PENDING 请求未自动拒绝** | `AdoptionRepository.approveRequest()` | 产生幽灵数据，管理员看到已领养宠物的待处理请求 |
| 6 | **搜索 LiveData observer 累积泄漏** | `HomeFragment.java:125-129` | 每次搜索添加新 observer 但不移除旧 observer，导致重复刷新和内存泄漏 |
| 7 | **AddPet age 无格式验证** | `AddPetBottomSheet.java:141` | "abc" + " Years" = "abc Years" 存入数据库 |
| 8 | **registerForActivityResult 调用过早** | `AddPetBottomSheet.java:35-36` | 可能在某些设备上崩溃 |

### 🟢 体验打磨

| # | 问题 | 位置 |
|---|------|------|
| 9 | 无错误状态 UI（数据库异常时无提示） | 所有页面 |
| 10 | 无下拉刷新 | HomeFragment、FavoritesFragment |
| 11 | 审批/删除无确认弹窗（只有宠物删除有） | AdminRequestsFragment、AdminPetsFragment |
| 12 | 宠物详情无多图轮播 | PetDetailActivity |
| 13 | 管理员仪表盘卡片无点击跳转 | AdminDashboardFragment |
| 14 | 死代码 MainActivity / activity_main.xml | 项目根目录 |

---

## 五、修复建议（按优先级排序）

### 第 1 批：让核心功能完整

1. **修复签名保存** — 将 `SignatureView.getSignatureBitmap()` 写入文件，存路径到 `signaturePath`
2. **实现领养历史** — 在 `ProfileFragment` 中创建 Adapter，观察 `AdoptionRepository.getRequestsByUser()`，显示 `item_adoption_history.xml`
3. **隐藏或实现忘记密码** — 至少加 `Toast` 提示 "Coming soon"

### 第 2 批：数据安全保障

4. **审批加事务** — 用 `@Transaction` 或 Room `runInTransaction` 包裹 update + update
5. **审批后自动拒绝同宠物其他请求** — 在 `approveRequest` 中加一行 DAO 调用
6. **修复搜索 observer 泄漏** — 用一个持久 LiveData 引用或使用 `switchMap`
7. **AddPet age 验证** — 限制 inputType 为 `numberDecimal`

### 第 3 批：体验完善

8. 各页面添加错误状态 UI
9. 添加 SwipeRefreshLayout
10. 管理员操作（审批/拒绝）加 AlertDialog 确认
11. 移除死代码 MainActivity
12. 详情页图片轮播
13. 仪表盘卡片点击跳转

---

## 六、功能完整度评分

| 模块 | 评分 | 评语 |
|------|------|------|
| 登录/注册 | 8/10 | 缺忘记密码和记住我 |
| 宠物浏览 | 8/10 | 缺下拉刷新、错误态、observer 泄漏 |
| 宠物详情 | 7/10 | 缺多图、加载态、错误态 |
| 收藏 | 8/10 | 缺错误态，其余正常 |
| **领养申请** | **4/10** | **签名未保存，核心功能残缺** |
| 用户资料 | 6/10 | **领养历史缺失** |
| 编辑资料 | 9/10 | 验证已加，几乎完整 |
| 管理员仪表盘 | 6/10 | 缺加载态、跳转、错误态 |
| 管理员宠物管理 | 7/10 | 缺 age 验证、错误态 |
| 管理员请求管理 | 6/10 | 缺事务、确认弹窗、级联拒绝 |
| 暗色模式 | 9/10 | 颜色完整覆盖 |

**综合评分：6.5/10** — 主流程可走通，但两个核心功能（签名保存、领养历史）缺失导致闭环不完整。

---

*本报告由 AI 产品经理自动生成，聚焦功能完整性端到端闭环。*
