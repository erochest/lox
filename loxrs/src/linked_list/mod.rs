
pub struct LinkedList<T> {
    phantom: std::marker::PhantomData<T>,
}

impl<T> LinkedList<T> {
    pub fn new() -> Self {
        LinkedList { phantom: std::marker::PhantomData }
    }

    pub fn is_empty(&self) -> bool {
        true
    }

    pub fn push(&mut self, item: T) {
        unimplemented!()
    }

    pub fn prepend(&mut self, item: T) {
        unimplemented!()
    }

    pub fn insert_after(&mut self, item: T) {
        unimplemented!()
    }

    pub fn insert_before(&mut self, item: T) {
        unimplemented!()
    }

    pub fn delete(&mut self, item: T) {
        unimplemented!()
    }

    pub fn pop(&mut self) -> Option<T> {
        unimplemented!()
    }

    pub fn pop_head(&mut self) -> Option<T> {
        unimplemented!()
    }

    pub fn find(&self, item: T) -> Option<T> {
        unimplemented!()
    }
}
#[cfg(test)]
mod tests;