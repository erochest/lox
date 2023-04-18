use std::borrow::BorrowMut;
use std::cell::Cell;
use std::rc::Rc;

type NodePtr<T> = Option<Rc<Cell<Node<T>>>>;

struct Node<T> {
    value: T,
    next: NodePtr<T>,
    prev: NodePtr<T>,
}

pub struct LinkedList<T> {
    head: NodePtr<T>,
    tail: NodePtr<T>,
    length: usize,
}

impl<T> LinkedList<T> {
    pub fn new() -> Self {
        Self {
            head: None,
            tail: None,
            length: 0,
        }
    }

    pub fn push(&mut self, value: T) {
        let mut new_node = Rc::new(Cell::new(Node {
                    value,
                    next: None,
                    prev: None,
                }));

        match self.tail {
            Some(ref _old_tail ) => {
                todo!();
                // new_node.get_mut().prev = Some(Rc::clone(&old_tail));
                // old_tail.next = Some(Rc::clone(&new_node));
                // self.tail = Some(Rc::clone(&new_node));
            },
            None => {
                self.head = Some(Rc::clone(&new_node));
                self.tail = Some(Rc::clone(&new_node));
            },
        }

        self.length += 1;
    }
}

#[cfg(test)]
mod tests;