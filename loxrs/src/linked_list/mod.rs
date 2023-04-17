
type NodePtr<T> = Option<Box<Node<T>>>;

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
}

#[cfg(test)]
mod tests;