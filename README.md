# The Idea

KBProxy provides a simple protocol for anonymous peer-to-peer communication via a TCP proxy. This proxy uses [KeyedBits](http://github.com/unixpickle/KeyedBits/), a binary format used for encoding JSON-style objects, as a packet format for communications.

KBProxy utilizes "tags", unique binary strings, to match two clients with one another. Thus, a communications channel is established through a proxy when two clients identify themselves with the same tag. Once two clients are connected, they can send KeyedBits objects back and forth through the proxy; all this is without any knowledge of the other client's hostname or IP address.

# Implementations

I will add any new implementations of KBProxy to this repository&ndash;that is, implementations which I create. Currently, I've been using a Java implementation which I wrote in the course of a few hours. This implementation uses the existing Java KeyedBits library (also authored by myself). However, I highly encourage anybody who's interested to write a KBProxy server in their own language of choice. There are already KeyedBits implementations for Objective-C, Ruby, JS, and Java!