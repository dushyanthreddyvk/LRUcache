# Mini Redis Clone

Plain Java mini Redis-style cache using sockets, an in-memory `HashMap`-based store, TTL expiration, LRU eviction, persistence, multithreading, and a Swing demo UI.

## Compile

```bash
javac -d out src/miniredis/*.java
```

## Run Swing Demo

```bash
java -cp out miniredis.SwingFrontend
```

The Swing app starts an embedded socket server on port `6379`.

## Run Server Only

```bash
java -cp out miniredis.Server
```

Optional arguments:

```bash
java -cp out miniredis.Server <port> <maxEntries>
```

## Run Console Client

Start the server first, then run:

```bash
java -cp out miniredis.ConsoleClient localhost 6379
```

## Commands

```text
SET key value
SET key value ttlSeconds
GET key
DELETE key
EXIT
```

Values are intentionally simple single-token strings so the socket protocol stays presentation-friendly.

## Sample Input Values

Use these in the Swing UI or the console client.

### Basic SET and GET

```text
SET name Dushyanth
GET name
```

Expected result:

```text
Dushyanth
```

### Store Multiple Keys

```text
SET language Java
SET database Redis
SET project MiniRedis
GET language
GET database
GET project
```

### DELETE Example

```text
SET session abc123
GET session
DELETE session
GET session
```

Expected final result:

```text
NULL
```

### TTL Expiration Example

```text
SET otp 987654 5
GET otp
```

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
