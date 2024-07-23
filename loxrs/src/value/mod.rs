pub type Value = f64;

pub struct ValueArray {
    pub values: Vec<Value>,
}

impl ValueArray {
    pub fn new() -> ValueArray {
        ValueArray { values: Vec::new() }
    }

    pub fn write(&mut self, value: Value) {
        self.values.push(value);
    }

    pub fn get(&self, index: usize) -> Value {
        self.values[index]
    }
}

impl Default for ValueArray {
    fn default() -> Self {
        Self::new()
    }
}

pub fn print_value(value: f64) {
    print!("{}", value);
}
