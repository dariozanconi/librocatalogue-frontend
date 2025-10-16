---

## 💻 Frontend (JavaFX)


# LibroCatalogue – Frontend

This is the JavaFX frontend for **LibroCatalogue**, a full-stack project to manage and catalog books.

## Tech Stack
- JavaFX
- Apache HttpClient
- Apache POI
- Jackson (JSON)
- JWT Authentication
- Multi-tab UI (Books, Search, Collections, Login/Register, Exports, ...)

## Project Structure
- `controller/` → UI controllers (HomeController, BooksTabController, …)
-  `task/` → JavaFX Tasks (Retrieve Books Task, Retrieve User Task, …)
- `service/` → Internal Logic (Http calls, GoogleBooks API, Apache POI, …)
- `model/` → Data models (Book, Collection, Tag, User, …)

  
## Screenshots


<img width="1350" height="717" alt="Screenshot 2025-10-16 104948" src="https://github.com/user-attachments/assets/d9760fe8-351d-40d7-bfee-128aaaf8ff54" />

#

<img width="1348" height="718" alt="Screenshot 2025-10-16 105249" src="https://github.com/user-attachments/assets/cd42ca53-e2ca-4ea1-8d96-ea204d792bf7" />

#

<img width="1351" height="718" alt="Screenshot 2025-10-16 105040" src="https://github.com/user-attachments/assets/f9321782-6f6e-47cb-92e7-26afca45b9e6" />

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


