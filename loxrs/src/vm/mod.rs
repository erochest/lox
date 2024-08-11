use std::convert::TryFrom;

use log;

use crate::chunk::{Chunk, OpCode};
use crate::compiler;
use crate::debug::dissassemble_instruction;
use crate::error::Result;
use crate::value::Value;

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

pub struct VM {
    pub stack: [Value; STACK_MAX],
    pub stack_top: usize,
}

impl VM {
    pub fn new() -> VM {
        VM {
            stack: [0.0; STACK_MAX],
            stack_top: 0,
        }
    }

    pub fn reset(&mut self) {
        self.stack_top = 0;
    }

    pub fn interpret<S: AsRef<str>>(&mut self, source: S) -> Result<()> {
        let mut chunk = compiler::compile(source.as_ref())?;
        self.run(&mut chunk)
    }

    fn run(&mut self, chunk: &mut Chunk) -> Result<()> {
        loop {
            if log::max_level() >= log::Level::Trace {
                self.print_stack();
                dissassemble_instruction(chunk, chunk.ip);
            }

            if chunk.at_end() {
                break;
            }
            let instruction = chunk.read_op_code();
            match OpCode::try_from(instruction)? {
                OpConstant => {
                    let constant = chunk.read_constant();
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
    }

    fn print_value(&self, value: f64) {
        print!("{}", value);
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
}

impl Default for VM {
    fn default() -> Self {
        Self::new()
    }
}
