pub enum OpCode {
    OpReturn,
}

// create a struct to represent a chunk of bytecode
pub struct Chunk {
    // store the bytecode in a vector
    code: Vec<u8>,
}

/// Create a new chunk
impl Chunk {
    pub fn new() -> Chunk {
        Chunk { code: Vec::new() }
    }

    /// Write a byte to the chunk
    pub fn write(&mut self, byte: u8) {
        self.code.push(byte);
    }

    /// Get the byte at the given index
    pub fn get(&self, index: usize) -> u8 {
        self.code[index]
    }
}

impl Default for Chunk {
    fn default() -> Self {
        Self::new()
    }
}
