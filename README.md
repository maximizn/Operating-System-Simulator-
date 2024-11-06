**Project Overview** (On going assignment, finish date: Dec 1, 2024)

This project implements a simplified operating system in Java, focusing on memory management and inter-process communication (IPC). 
The primary goal is to simulate core operating system concepts like virtual memory translation, TLB (Translation Lookaside Buffer) management, and IPC mechanisms between userland processes.

**Virtual Memory Management:**

Implements virtual-to-physical address translation using a page table and TLB for caching mappings.
Includes page allocation and TLB handling, with functions to update and clear the TLB.
Demonstrates mapping verification and error handling for missed mappings.

**Inter-Process Communication (IPC):**

Ping and Pong processes simulate message exchange between processes.
Uses a basic kernel-managed message system where processes send and receive messages through a message queue.
Message flow is demonstrated with incremented data exchanges and debug outputs for clear tracing.

**Key Classes**

**Kernel:** Manages process scheduling, TLB updates, and IPC functions. The kernel controls access to hardware-like resources, such as TLB and physical memory.
**Scheduler:** Implements process scheduling, queue management, and state tracking for processes. The scheduler is also responsible for waiting and restoring processes as they interact via messages.
**Process Management:** Includes classes for the Ping and Pong processes that continuously exchange messages, simulating inter-process communication and synchronization.
**TLB Management: **Updates and verifies TLB entries for address translation, with random replacement on TLB miss.
Current Features

**Page Table and TLB Handling:**

**updateTlb **adds a virtual-to-physical page mapping to the TLB, replacing entries if the TLB is full.
**clearTlb **resets TLB entries using a for loop, ensuring no residual mappings.
**GetMapping** handles virtual page requests, updating page tables, and verifying TLB consistency.

**Inter-Process Messaging System:**

Kernel handles messages between processes, allowing Ping and Pong to send and receive messages with incremental data.
Demonstrates message queuing, descheduling/waking of processes, and message validation.
