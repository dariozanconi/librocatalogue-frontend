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
<img width="1176" height="717" alt="Screenshot 2025-10-01 115053" src="https://github.com/user-attachments/assets/a1301e93-1376-417f-b40e-66614b303d28" />


#

<img width="1178" height="718" alt="Screenshot 2025-10-01 115127" src="https://github.com/user-attachments/assets/61e2deec-df68-4c5f-aa5e-6883f520606b" />

#

<img width="717" height="455" alt="Screenshot 2025-10-01 114246" src="https://github.com/user-attachments/assets/38bbe601-6e73-4b12-9cc4-72a91213eee2" />


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


