# Mini Redis Clone

A lightweight, feature-rich in-memory key-value cache implemented in Java. This project demonstrates core Redis functionality including socket programming, LRU eviction, TTL expiration, persistence, and concurrent client handling—ideal for interviews and system design portfolios.

## 🚀 Features

- **In-Memory Cache**: Fast HashMap-based key-value storage with O(1) lookups
- **LRU Eviction**: Automatic removal of least recently used entries when capacity is exceeded
- **TTL Support**: Time-to-live expiration for keys with background cleanup
- **Persistence**: Automatic serialization to disk (`data/cache-store.bin`) after each mutation
- **Multi-Client Support**: Concurrent client handling with thread-safe operations
- **GUI Dashboard**: Interactive Swing frontend with real-time cache visualization
- **Console Client**: CLI interface for manual socket testing
- **Configurable**: Custom port, max entries, and persistence settings

## 📋 Quick Start

### Compilation

```bash
javac -d out src/miniredis/*.java
```

### Run with GUI (Recommended)

```bash
java -cp out miniredis.SwingFrontend
```

The Swing app starts an embedded server on port `6379` and provides a visual interface.

**[Screenshot of Swing UI would go here]**

### Run Server Only

```bash
java -cp out miniredis.Server
# Or with custom settings:
java -cp out miniredis.Server 6379 1000
```

### Connect with Console Client

In a separate terminal:

```bash
java -cp out miniredis.ConsoleClient localhost 6379
```

**[Screenshot of Console Client would go here]**

## 💻 Commands

| Command | Description | Example |
|---------|-------------|---------|
| `SET key value` | Store a value | `SET name Dushyanth` |
| `SET key value ttl` | Store with TTL (seconds) | `SET otp 123456 300` |
| `GET key` | Retrieve a value | `GET name` |
| `DELETE key` | Remove a key | `DELETE session` |
| `EXIT` | Close connection | `EXIT` |

## 📚 Usage Examples

### Basic Operations

```text
SET username alice
GET username
→ alice

SET score 100
GET score
→ 100
```

### TTL Expiration

```text
SET temp_token abc123 10
GET temp_token
→ abc123
# Wait 10 seconds...
GET temp_token
→ NULL (expired)
```

### LRU Eviction (with max 3 entries)

```text
SET key1 value1
SET key2 value2
SET key3 value3
# Cache is full
SET key4 value4  # This triggers LRU eviction of key1
GET key1
→ NULL (evicted)
```

### Multiple Keys

```text
SET language Java
SET database Redis
SET framework Spring
GET language
→ Java
GET database
→ Redis
```

## 🏗️ Architecture

```
Mini Redis Clone
├── Server: Accepts socket connections & manages clients
├── ClientHandler: Processes commands for each connected client
├── CommandProcessor: Parses & validates commands
├── CacheStorage: Core cache logic with TTL & persistence
├── LRUCache: LinkedHashMap-based eviction policy
├── CacheEntry: Individual cache entries with TTL metadata
└── SwingFrontend: GUI for interactive cache management
```

### Key Design Patterns

| Component | Pattern | Purpose |
|-----------|---------|---------|
| **LRUCache** | Inheritance | Extends LinkedHashMap for access-order tracking |
| **CacheStorage** | Singleton | Single source of truth for cache state |
| **CacheEventListener** | Observer | Decoupled logging and UI updates |
| **ClientHandler** | Thread Pool | Concurrent multi-client support |

## ⚡ Performance Analysis

| Operation | Time Complexity | Notes |
|-----------|-----------------|-------|
| SET | O(1) | HashMap insertion |
| GET | O(1) | HashMap lookup + access-order update |
| DELETE | O(1) | HashMap removal |
| LRU Eviction | O(1) | LinkedHashMap identifies eldest entry |
| TTL Cleanup | O(n) | Scans all entries for expiration |
| Persistence | O(n) | Serializes entire cache |

## 🔧 Technical Stack

- **Language**: Java 8+
- **Concurrency**: ThreadPoolExecutor, synchronized blocks
- **Data Structures**: HashMap, LinkedHashMap, ArrayList
- **Networking**: ServerSocket, Socket, BufferedReader, PrintWriter
- **Persistence**: ObjectInputStream/ObjectOutputStream
- **UI**: Swing (JFrame, JTable, JTextArea)

## 📖 Core Concepts Demonstrated

- **Socket Programming**: TCP client-server communication over port 6379
- **Multithreading**: Thread-per-client model with shared cache synchronization
- **LRU Cache**: LinkedHashMap with access-order enabled for automatic eviction
- **TTL Expiration**: Background daemon thread for time-based key invalidation
- **Serialization**: ObjectOutputStream for persistence to `data/cache-store.bin`
- **OOP Principles**: Encapsulation, abstraction, inheritance, and single responsibility

## 📁 Project Structure

```
.
├── src/miniredis/
│   ├── Server.java              # TCP server & client listener
│   ├── ClientHandler.java       # Per-client request processor
│   ├── CommandProcessor.java    # Command parsing & validation
│   ├── CacheStorage.java        # Core cache with TTL & persistence
│   ├── LRUCache.java            # LinkedHashMap-based eviction
│   ├── CacheEntry.java          # Value wrapper with TTL metadata
│   ├── CacheEventListener.java  # Event callback interface
│   ├── SwingFrontend.java       # GUI dashboard
│   └── ConsoleClient.java       # CLI for manual testing
├── data/
│   └── cache-store.bin          # Persisted cache (created at runtime)
├── README.md                    # This file
└── PROJECT_EXPLANATION.txt      # Detailed documentation
```

## 🎯 Real-World Use Cases

- **Session Management**: Cache user sessions with automatic TTL expiration
- **Rate Limiting**: Store rate-limit counters with expiration
- **OTP Verification**: Temporary storage for one-time passwords
- **Database Query Cache**: Reduce repeated database calls
- **Message Queue**: Fast temporary storage for in-flight messages

## 🚀 Future Enhancements

- [ ] Support multi-token values using RESP protocol
- [ ] Add command authentication/ACL
- [ ] Batch persistence instead of per-operation writes
- [ ] Metrics dashboard (hit rate, eviction count, memory usage)
- [ ] Replication & clustering
- [ ] Unit & integration tests
- [ ] REST API gateway
- [ ] Web-based dashboard

## 📝 Interview Talking Points

1. **Why HashMap for fast lookup?** → Hash functions distribute keys uniformly, giving O(1) average lookup
2. **Why LinkedHashMap for LRU?** → Combines hash table performance with linked list to track access order
3. **How does synchronization protect data?** → synchronized blocks ensure atomic operations during concurrent access
4. **How does TTL work?** → Background thread periodically scans and removes expired keys
5. **Why socket programming?** → Demonstrates low-level networking and protocol design

## 📄 License

MIT License - Feel free to use for learning and portfolio projects.

---

**Note**: To add screenshots, run the project and capture:
1. Swing GUI in action (cache operations, logs)
2. Console client session
3. Multiple clients connecting simultaneously

Then create an `images/` folder and reference them as `![Swing UI](images/swing-ui.png)`

Wait 5 seconds, then run:

```text
GET otp
```

Expected final result:

```text
NULL
```

The log panel should show a key expiration message.

### LRU Eviction Example

The default cache limit is `5` entries. Run:

```text
SET user1 Alice
SET user2 Bob
SET user3 Charlie
SET user4 Divya
SET user5 Ethan
GET user1
SET user6 Farah
```

Because `GET user1` makes `user1` recently used, adding `user6` should evict the least recently used remaining key, usually `user2`.

### Swing UI Field Examples

For SET with no TTL:

```text
Key: city
Value: Hyderabad
TTL: leave empty
```

For SET with TTL:

```text
Key: token
Value: xyz789
TTL: 10
```

For GET or DELETE:

```text
Key: city
Value: leave empty
TTL: leave empty
```
