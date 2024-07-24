use std::convert::TryFrom;

use log;

use crate::chunk::{Chunk, OpCode};
use crate::debug::dissassemble_instruction;
use crate::error::{Error, Result};
use crate::value::Value;

use Error::*;
use OpCode::*;

const STACK_MAX: usize = 256;

pub struct VM<'a> {
    pub chunk: Option<&'a Chunk>,
    pub ip: usize,
    pub stack: [Value; STACK_MAX],
    pub stack_top: usize,
}

impl<'a> VM<'a> {
    pub fn new() -> VM<'a> {
        VM {
            chunk: None,
            ip: 0,
            stack: [0.0; STACK_MAX],
            stack_top: 0,
        }
    }

    pub fn reset(&mut self) {
        self.stack_top = 0;
    }

    pub fn interpret(&'a mut self, chunk: &'a Chunk) -> Result<()> {
        self.chunk = Some(chunk);
        self.ip = 0;
        self.run()
    }

    pub fn run(&mut self) -> Result<()> {
        if let Some(chunk) = self.chunk {
            loop {
                if log::max_level() >= log::Level::Trace {
                    self.print_stack();
                    dissassemble_instruction(chunk, self.ip);
                }

                if self.at_end() {
                    break;
                }
                let instruction = self.read_op_code(chunk);
                match OpCode::try_from(instruction)? {
                    OpConstant => {
                        let constant = self.read_constant(chunk);
                        self.push(constant);
                    }
                    OpNegate => {
                        let value = self.pop();
                        self.push(-value);
                    }
                    OpReturn => {
                        let value = self.pop();
                        self.print_value(value);
                        println!();
                    }
                }
            }
            Ok(())
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

    #[inline]
    fn push(&mut self, value: Value) {
        self.stack[self.stack_top] = value;
        self.stack_top += 1;
    }

    #[inline]
    fn pop(&mut self) -> Value {
        self.stack_top -= 1;
        self.stack[self.stack_top]
    }

    fn print_stack(&self) {
        print!("          ");
        for i in 0..self.stack_top {
            print!("[{}]", self.stack[i]);
        }
        println!();
    }

    fn at_end(&self) -> bool {
        self.ip >= self.chunk.unwrap().code.len()
    }
}

impl<'a> Default for VM<'a> {
    fn default() -> Self {
        Self::new()
    }
}
