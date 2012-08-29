# Prerequisits

## Packet Format

KBProxy rests completely on top of a binary format called [*KeyedBits*](https://github.com/unixpickle/KeyedBits/). Every packet sent to and from a KBProxy server is encoded as a single KeyedBits object and decoded as such.

KeyedBits is a binary format which can be used to represent JSON-style dictionary data. In addition, KeyedBits supports raw binary data as a built-in datatype. This makes it the perfect format for a lightweight network protocol that sits on top of a lossless communications channel (e.g. TCP).

## Notation

For the purpose of simplicity, the JSON format is used throughout this specification to represent KeyedBits objects. For those unfamiliar with JSON, here is a basic rundown:

* Numbers are represented as their plain numerical value. **Examples:** `10`, `3.1415`
* Strings are represented as a series of characters in double quotes. A quotation mark within a string is escaped with a backslash, as are backslashes and Unicode characters. **Example:** `"\"test\" is a string"`
* NULLs are represented by the keyword `null`.
* Arrays are enclosed by `[` and `]` with values delimited by `,`.
* A dictionary/hash/associative array is enclosed by `[` and `]` with each key followed by a : and and an object. **Example:** `["age": 10, "name": "james"]`. **Note:** KeyedBits will not allow a dictionary key that is not an ASCII string.

In addition, binary data is represented as hexadecimal enclosed in `<` and `>`.


# KBProxy

## General Idea + Example

KBProxy provides a simple mechanism which allows two anonymous clients to communicate with each other. This is done using *tags*, a sort of nickname mechanism. Take the following example of a typical communications scenario:

First, *Client A* connects to the proxy and specifies a tag. If this tag is not already in use, the proxy informs *Client A* that it now owns the tag. Next, *Client B* connects to the proxy and specifies the same tag. Since *Client A* is the only other client with this tag, the proxy assumes that *Client A* and *Client B* wish to establish communication, and thus informs both clients that there is an active client on the other end.

These clients can then communicate, sending KeyedBits packets amongst one-another. Finally, when one client wishes to disconnect, it can either gracefully unregister itself or simply terminate the (TCP) connection. The other client, still owning its tag, will then be notified that there is no longer another client on the other end; it now owns the tag.

As is apparent from the above example, a KBProxy makes it simple for two clients from anywhere in the world to connect and establish communication. This can be necessary for getting past firewalls, especially in applications made specifically for end-users: simply entering a nickname is much easier than asking your remote buddy for his/her IP address.

## Terms

* The *server* - a KBProxy server which accepts connections on a lossless binary communications channel.
* A *client* - a KBProxy client which opens a connection to a *server* and utilizes its purposes (typically for an end-user).
* A *tag* - a tag which represents either one or two actively connected clients. Tags are represented as binary data.

## Packets

### Tag Identify Packet

**Direction:** client -> server

**Description**: A packet requesting that a client be assigned a certain tag. If this tag is registered by a single other client, sending this packet will establish a connection to the other client. If the client was already identified with another tag, the client will lose ownership of that tag upon sending this packet, regardless of whether or not the requested tag is available.

	[
	 "type": "tag",
	 "tag": <AABBCCEEDDFF…>
	]

### Tag Taken Packet

**Direction:** server -> client

**Description**: A packet which indicates that two clients are already actively registered with the specified tag.

	[
	 "type": "taken",
	 "tag": <AABBCCEEDDFF…>
	]

### Tag Owned

**Direction:** server -> client

**Description**: A packet which indicates that the client now owns a tag. This will be sent to a client when it identifies itself with a tag that was not in use by any other clients. Moreover, this packet will be sent to a client when the other client with the same tag has disconnected.

	[
	 "type": "owned",
	 "tag": <AABBCCEEDDFF…>
	]

### Connection Established

**Direction:** server -> client

**Description**: A packet sent to two clients (who share the same tag) indicating that they may now send data between one another.

	[
	 "type": "connected",
	 "tag": <AABBCCEEDDFF…>
	]

### Transmit Client Data

**Direction:** client -> server

**Description**: A packet sent by one of the two connected clients which specifies data to be delivered to the other. The object contained in this packet can be any KeyedBits datatype, such as a dictionary or a binary buffer.

	[
	 "type": "transmit",
	 "object": anything
	]

### Incoming Client Data

**Direction:** server -> client

**Description**: A packet sent to one of the two connected clients which contains data sent in a transmit packet by the other client.

	[
	 "type": "incoming",
	 "object": anything
	]

### Unregister

**Direction:** client -> server

**Description**: A packet sent to the server to indicate that a client wishes to unregister its tag without disconnecting from the communications channel.

	["type": "unregister"]

### Error

A miscellaneous error sent by the server to the client.

	[
	 "type": "error",
	 "message": "Something",
	 "number": (number)
	]

The current error numbers include:

* 1 - Internal error
* 2 - Remote end not connected