use std::convert::TryFrom;

use crate::chunk::{Chunk, OpCode};
use crate::error::{Error, Result};

use Error::*;
use OpCode::*;

pub struct VM<'a> {
    pub chunk: Option<&'a Chunk>,
    pub ip: usize,
}

impl<'a> VM<'a> {
    pub fn new() -> VM<'a> {
        VM { chunk: None, ip: 0 }
    }

    pub fn interpret(&'a mut self, chunk: &'a Chunk) -> Result<()> {
        self.chunk = Some(chunk);
        self.ip = 0;
        self.run()
    }

    pub fn run(&mut self) -> Result<()> {
        if let Some(chunk) = self.chunk {
            loop {
                let instruction = self.read_op_code(chunk);
                match OpCode::try_from(instruction)? {
                    OpConstant => {
                        let constant = self.read_constant(chunk);
                        self.print_value(constant);
                        println!();
                    }
                    OpReturn => {
                        return Ok(());
                    }
                }
            }
        } else {
            Err(MissingChunkError)
        }
    }

    fn print_value(&self, value: f64) {
        print!("{}", value);
    }

    #[inline]
    fn read_op_code(&mut self, chunk: &'a Chunk) -> u8 {
        self.ip += 1;
        chunk.code[self.ip - 1]
    }

    #[inline]
    fn read_constant(&mut self, chunk: &'a Chunk) -> f64 {
        let constant = self.read_op_code(chunk);
        chunk.constants[constant as usize]
    }
}

impl<'a> Default for VM<'a> {
    fn default() -> Self {
        Self::new()
    }
}
