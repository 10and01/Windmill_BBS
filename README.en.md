# Windmill_BBS 🌪️

> An enterprise BBS forum system with Neumorphism design and dual light/dark themes

<p align="center">
  <img src="Windmill_Icon.ico" width="64" alt="Windmill_BBS Logo">
</p>

<p align="center">
  <a href="README.md">🇨🇳 中文</a> | <a href="README.en.md">🇺🇸 English</a>
</p>

---

## 📖 Introduction

Windmill_BBS is an enterprise-level BBS forum system built on **Java Servlet + JDBC**. The frontend is implemented with pure HTML/CSS/JS, featuring a **Neumorphism** design style with **light/dark dual-theme support**, fresh color palettes, and smooth interactive experiences.

🚀 **Live Demo**: https://windmillbbs-production.up.railway.app/

---

## ✨ Features

### User Features
- 🔐 **User System**: Registration, login, logout, profile management, avatar upload
- 📝 **Post Module**: Create, edit, delete, search posts; like, top, elite
- 💬 **Reply Module**: Floor-based replies, image replies, like interactions
- 🚨 **Report System**: Report posts and replies; admin backend processing
- 🎨 **Dual Themes**: One-click switch between light/dark modes with preference persistence

### Admin Features
- 📊 **Admin Dashboard**: Post management, board management, report handling
- 🛡️ **Permission Control**: Board-level publish permissions (e.g., "Announcements" board is admin-only)
- ⚡ **Post Operations**: Top, elite, delete, and other quick actions

### UI Design
- 🖼️ **Neumorphism Style**: Dual shadows, inner/outer convex effects, soft rounded corners
- 🌗 **Light & Dark Themes**: Light theme uses #F5F3ED warm white + cyan accents; dark theme uses #1c1c1e
- 🔍 **Image Preview**: Click images in posts and replies to view fullscreen
- ⏱️ **Timeline**: Right-side timeline on post detail page with floor navigation and date markers

---

## 🖼️ Screenshots

### Light Mode (Day Mode)
<p align="center">
  <img src="day.png" width="80%" alt="Light Mode">
</p>

### Dark Mode (Dark Mode)
<p align="center">
  <img src="dark.png" width="80%" alt="Dark Mode">
</p>

---

## 🏗️ Architecture Design

<p align="center">
  <img src="architecture.png" width="90%" alt="Architecture Diagram">
</p>

The project follows a classic **MVC layered architecture**:

| Layer | Components | Responsibility |
|-------|-----------|----------------|
| **Presentation** | HTML/CSS/JS Pages | UI rendering, user interaction, theme switching |
| **Filter** | EncodingFilter, LoginFilter, AdminAuthFilter | Encoding, authentication, admin privilege check |
| **Controller** | Servlets (User/Post/Reply/Board/Admin/Upload/Like/Report) | Request routing, parameter parsing, JSON response |
| **Service** | Service Classes | Business logic, transaction control |
| **DAO** | DAO Classes | SQL execution, result mapping |
| **Database** | MySQL 8.0 | Data persistence |
| **Utility** | DBUtil, JsonUtil, BackupTask | Database connection, JSON serialization, scheduled backup |

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | HTML5 + CSS3 + JavaScript (Vanilla, no frameworks) |
| Backend | Java Servlet + JDBC |
| Database | MySQL 8.0 |
| Connection Pool | Alibaba Druid |
| Build Tool | Maven 3.9+ |
| Server | Tomcat 9.0+ |

---

## 📁 Project Structure

```
Windmill_BBS/
├── src/main/java/com/enterprise/bbs/    # Java backend source
│   ├── controller/                      # Servlet controllers
│   ├── dao/                             # Data access layer
│   ├── model/                           # Entity classes
│   ├── service/                         # Business logic layer
│   ├── filter/                          # Filters
│   └── util/                            # Utilities
├── src/main/webapp/                     # Frontend pages
│   ├── index.html                       # Home page
│   ├── login.html                       # Login page
│   ├── register.html                    # Register page
│   ├── post/detail.html                 # Post detail
│   ├── user/home.html                   # User home
│   ├── user/profile.html                # Profile
│   ├── admin/dashboard.html             # Admin dashboard
│   ├── static/css/theme.css             # Global theme styles
│   ├── static/js/theme.js               # Theme switch script
│   └── uploads/                         # Upload directory
├── src/main/resources/                  # Config files
│   ├── db.properties                    # Database config
│   └── init.sql                         # DB init script
├── pom.xml                              # Maven config
├── README.md                            # Chinese documentation
├── README.en.md                         # English documentation
├── day.png                              # Light mode screenshot
├── dark.png                             # Dark mode screenshot
├── architecture.png                     # Architecture diagram
└── Windmill_Icon.ico                    # Site icon
```

---

## 🚀 Quick Deploy

### Requirements
- JDK 11+
- MySQL 8.0+
- Maven 3.9+
- Tomcat 9.0+

### 1. Clone

```bash
git clone <repo-url>
cd Windmill_BBS
```

### 2. Initialize Database

Edit `src/main/resources/db.properties` with your DB credentials:

```properties
url=jdbc:mysql://localhost:3306/enterprise_bbs?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
username=root
password=YOUR_PASSWORD
```

Run the init script:

```bash
mysql -u root -p < src/main/resources/init.sql
```

> If the database already exists, only run:
> ```sql
> ALTER TABLE t_board ADD COLUMN post_permission TINYINT NOT NULL DEFAULT 0;
> UPDATE t_board SET post_permission = 1 WHERE board_name = '公告通知';
> ```

### 3. Build

```bash
mvn clean package
```

The WAR file will be generated at `target/enterprise-bbs.war`.

### 4. Deploy to Tomcat

```bash
cp target/enterprise-bbs.war $TOMCAT_HOME/webapps/
```

Start Tomcat:

```bash
$TOMCAT_HOME/bin/startup.bat   # Windows
$TOMCAT_HOME/bin/startup.sh    # Linux/Mac
```

### 5. Access

```
http://localhost:8080/enterprise-bbs/index.html
```

Default admin account: `admin` / `admin123`

---

## ⚠️ Notes

1. **Upload Directory**: Uploaded images are stored in the `uploads/` folder under the Tomcat deployment directory. **Please back up this folder before redeploying the WAR**, otherwise historical images will be lost.

2. **Recommended**: Configure a virtual directory in `server.xml` to map uploads to an external path:
   ```xml
   <Context docBase="D:/bbs-uploads" path="/enterprise-bbs/uploads" />
   ```

---

## 📄 License

This project is for educational and communication purposes only.
