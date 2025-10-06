---

## 💻 Frontend (JavaFX)


# LibroCatalogue – Frontend

This is the JavaFX frontend for **LibroCatalogue**, a full-stack project to manage and catalog books.

## Tech Stack
- JavaFX
- Apache HttpClient
- Jackson (JSON)
- JWT Authentication
- Multi-tab UI (Books, Search, Collections, Login/Register)

## Project Structure
- `controller/` → UI controllers (HomeController, BooksTabController, …)
- `service/` → API calls (BookTasks, CollectionTasks, AuthTasks, …)
- `model/` → Data models (Book, Collection, Tag, User, …)
- 
## Screenshots

<img width="1353" height="722" alt="Screenshot 2025-10-06 105132" src="https://github.com/user-attachments/assets/fac436fc-ed59-4a93-9bbf-70d5e0abfa8a" />

#

<img width="1352" height="715" alt="Screenshot 2025-10-06 105234" src="https://github.com/user-attachments/assets/5751a418-c0a6-4659-afa0-8c1cc31c8ddf" />


#

<img width="1353" height="717" alt="Screenshot 2025-10-06 105317" src="https://github.com/user-attachments/assets/d95fa818-b75d-4ba4-bd0d-187a95553cf3" />


#

<img width="851" height="567" alt="Screenshot 2025-09-22 095240" src="https://github.com/user-attachments/assets/f5fd7dd1-d55f-4727-a18a-2744dba33d3f" />

#

## Setup

### Prerequisites
- Java 17+
- Maven
- Backend running (Spring Boot)

### Configuration
In `DomainConstant.java`, you can switch between local and public API:

```java
public class DomainConstant {
    public static final String DOMAIN_LOCAL= "http://localhost:8080/api";
	public static final String DOMAIN_PUBLIC = System.getProperty("DOMAIN_PUBLIC");
}
```
And set the Property Variables as VM Arguments
```env
-DGOOGLE_APIKEY=<your-key>
-DDOMAIN_PUBLIC=https://youserver.com/api
```


