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
<img width="1365" height="716" alt="Screenshot 2025-09-22 095545" src="https://github.com/user-attachments/assets/8536ebcb-b059-4f16-8c6b-289e6b086267" />

#

<img width="1365" height="717" alt="Screenshot 2025-09-22 095523" src="https://github.com/user-attachments/assets/55b8dc8f-01a9-4dd3-83c3-aff3571b39e4" />

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
And set the Property Variables als VM Arguments
```env
-DGOOGLE_APIKEY=<your-key>
-DDOMAIN_PUBLIC=https://youserver.com/api
```


