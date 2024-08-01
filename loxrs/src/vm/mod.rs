use std::convert::TryFrom;

use log;

use crate::chunk::{Chunk, OpCode};
use crate::compiler;
use crate::debug::dissassemble_instruction;
use crate::error::{Error, Result};
use crate::value::Value;

use Error::*;
use OpCode::*;

macro_rules! binary_op {
    ($vm:ident, $op:tt) => {
        {
            let b = $vm.pop();
            let a = $vm.pop();
            $vm.push(a $op b);
        }
    };
}

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

    pub fn interpret<S: AsRef<str>>(&mut self, source: S) -> Result<()> {
        compiler::compile(source.as_ref())?;
        Ok(())
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
                    OpAdd => binary_op!(self, +),
                    OpSubtract => binary_op!(self, -),
                    OpMultiply => binary_op!(self, *),
                    OpDivide => binary_op!(self, /),
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
