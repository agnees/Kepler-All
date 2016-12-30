package com.kepler.async;

import java.util.concurrent.atomic.AtomicReference;

public class ConcurrentStack<T> {

	class Node {
		T item;
		Node next;
		
		Node(T item) {
			this.item = item;
			this.next = null;
		}
	}
	
	private AtomicReference<Node> head = new AtomicReference<>(null);
	
	public ConcurrentStack() {
		
	}
	
	public void push(T item) {
		Node t = new Node(item);
		Node o = null;
		boolean suc = false;
		while (!suc) {
			o = head.get();
			suc = head.compareAndSet(o, t);
		}
		t.next = o;
	}
	
	public T pop() {
		if (head.get() == null) {
			return null;
		}
		boolean suc = false;
		Node o = null, n = null;
		while (!suc) {
			o = head.get();
			if (o == null) {
				return null;
			}
			n = o.next;
			suc = head.compareAndSet(o, n);
		}
		o.next = null;
		return o.item;
	}
	
}
