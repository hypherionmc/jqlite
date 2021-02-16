# JQLite - Easy SQLite library for Java

**WARNING: I DO NOT CONSIDER THIS LIBRARY COMMERCIAL READY. PLEASE USE AT YOUR OWN RISK!!! YOU HAVE BEEN WARNED!**

#### What is this library?
This library is intended to make working with SQLite databases in java projects easier. It provides some easy to use features which could save you a lot of development time.

#### How do I use this library?
To use this library, you first need to add it.

Add the following to the repositories block of your `build.gradle` file:

```maven { url 'https://maven.hypherionmc.me' }```

Then add the following to dependencies:

```gradle
compile 'me.hypherionmc:JQLite:1.0.1'
```
No need for any other dependencies as they are included in the library. Find the latest versions [HERE](https://maven.hypherionmc.me/me/hypherionmc/JQLite/)

#### Getting Started

To get started, you first need to create a new `JQLite` instance.

```java
// Replace testdb with your database name
private final DatabaseEngine engine = new DatabaseEngine("testdb");
```

Next, you need to create a "Table Class".

```java
public class MyTableName extends SQLiteTable {

    // This is the primary, autoincrementing key; 
    // THIS MUST ALWAYS BE AT THE TOP ABOVE EVERYTHING ELSE!!!
    @SQLCOLUMN(type = SQLCOLUMN.Type.PRIMARY)
    private int id;

    @SQLCOLUMN(type = SQLCOLUMN.Type.VARCHAR, maxSize = 255)
    private String name; // A basic VARCHAR column limited to 255 characters

    @SQLCOLUMN(type = SQLCOLUMN.Type.BOOLEAN)
    private boolean isRegistered; // A basic Boolean column

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setRegistered(boolean registered) {
        isRegistered = registered;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public int getId() {
        return id;
    }

}
```

Then you need to register the Table to the Database engine using ```engine.registerTable(myTableName);```

Each class extending `SQLiteTable` comes with the following methods:

* insert
* update
* fetchAll
* fetch
* delete

#### Examples

Basic insert example

```java
MyTableName myTable = new MyTableName();
engine.registerTable(myTable); // This only needs to be done once!
myTable.setName("John Doe");
myTable.setRegistered(true);

myTable.insert();
```

Basic update example

```java
MyTableName myTable = new MyTableName();
myTable.fetch("name = 'John Doe'");
myTable.setRegistered(false);

myTable.update();
```

Basic delete example

```java
MyTableName myTable = new MyTableName();
myTable.fetch("name = 'John Doe'");

myTable.delete();
```

Basic fetchAll example

```java
MyTableName myTable = new MyTableName();
List<MyTableName> tableList = myTable.fetchAll();

for (MyTableName myTable1 : tableList) {
    System.out.println(myTable1.getName());
}
```

Basic fetchAll with filter example

```java
MyTableName myTable = new MyTableName();
List<MyTableName> tableList = myTable.fetchAll("name = 'John Doe'");

for (MyTableName myTable1 : tableList) {
    System.out.println(myTable1.getName());
}
```

If you need help with this library, please join my [discord](https://discord.gg/PdVnXf9) or open an Issue above