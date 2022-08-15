## JQLite - An Easy SQLite Java Wrapper

***

### Note: This library has been successfully used in Discord Bots and Minecraft mods. 

From V1.x, Version `1.0.4` is the only properly working version. 

***

#### What is this library?
This library is intended to make working with SQLite databases in java projects easier. It provides some easy to use features which could save you a lot of development time.


#### How do I use this library?
To use this library, you first need to add it.

Add the following to the repositories block of your `build.gradle` file:

```gradle
// For latest releases
maven { url 'https://maven.firstdarkdev.xyz/releases' }

// For Snapshots/Betas
maven { url 'https://maven.firstdarkdev.xyz/snapshots' }
```

&nbsp;

![](https://maven.firstdarkdev.xyz/api/badge/latest/releases/me/hypherionmc/JQLite?color=40c14a&name=Latest%20Stable) ![](https://maven.firstdarkdev.xyz/api/badge/latest/snapshots/me/hypherionmc/jqlite/JQLite?color=FF0000&name=Latest%20Snapshot)

Then add the following to dependencies:

```gradle
// Replace VERSION with one from above
implementation 'me.hypherionmc.jqlite:JQLite:VERSION'
```
No need for any other dependencies as they are included in the library.

---

#### Getting Started

To get started, you first need to create a new `JQLite` instance.

```java
// Replace testdb with your database name
private final SQLiteDatabase database = new SQLiteDatabase("testdb");
```

Next, you need to create a "Table Class".

```java
public class MyTableName extends SQLiteTable {

    // This is the primary, autoincrementing key; 
    // THIS MUST ALWAYS BE AT THE TOP ABOVE EVERYTHING ELSE!!!
    @SQLColumn(SQLColumn.Type.PRIMARY)
    private int id;

    @SQLColumn(value = SQLColumn.Type.VARCHAR, maxSize = 255)
    private String name; // A basic VARCHAR column limited to 255 characters

    @SQLColumn(SQLColumn.Type.BOOLEAN)
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

Then you need to register the Table to the Database engine using 
```
// Single Table
database.registerTable(myTableName);

// Multiple Tables
database.registerTable(table1, table2, table3);
```

Each class extending `SQLiteTable` comes with the following methods:

* insert
* update
* fetchAll
* fetch
* delete
* insertUnique
* insertOrUpdate

#### Examples

Basic insert example

```java
MyTableName myTable = new MyTableName();
database.registerTable(myTable); // This only needs to be done once!
myTable.setName("John Doe");
myTable.setRegistered(true);

myTable.insert(database);
```

Basic update example

```java
MyTableName myTable = new MyTableName();
myTable.fetch("name = 'John Doe'");
myTable.setRegistered(false);

myTable.update(database);
```

Basic delete example

```java
MyTableName myTable = new MyTableName();
myTable.fetch("name = 'John Doe'");

myTable.delete(database);
```

Basic fetchAll example

```java
MyTableName myTable = new MyTableName();
List<MyTableName> tableList = myTable.fetchAll(database);

for (MyTableName myTable1 : tableList) {
    System.out.println(myTable1.getName());
}
```

Basic fetchAll with filter example

```java
MyTableName myTable = new MyTableName();
List<MyTableName> tableList = myTable.fetchAll(database, "name = 'John Doe'");

for (MyTableName myTable1 : tableList) {
    System.out.println(myTable1.getName());
}
```

Basic insertUnique example

```java
MyTableName myTable = new MyTableName();
myTable.setName("John Doe");
myTable.setRegistered(true);

// This will return TRUE if the entry was inserted, 
// or false, if a duplicate already exists
myTable.insertUnique(database, "name = 'John Doe'");
```

Basic insertOrUpdate example

```java
MyTableName myTable = new MyTableName();
myTable.setName("John Doe");
myTable.setRegistered(true);

// If a duplicate entry is found, it will be updated,
// otherwise it will be inserted
myTable.insertOrUpdate(database, "name = 'John Doe'");
```

If you need help with this library, please join my [discord](https://discord.gg/PdVnXf9) or open an Issue above
