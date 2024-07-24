use std::convert::From;
use std::error;
use std::fmt;
use std::io;
use std::result;

pub type Result<R> = result::Result<R, Error>;

#[derive(Debug)]
pub enum Error {
    MissingChunkError,
    InvalidOpCode(u8),
    CompileError,
    RuntimeError,
    IoError(io::Error),
}

use Error::*;

impl fmt::Display for Error {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            MissingChunkError => write!(f, "Missing chunk error"),
            InvalidOpCode(value) => write!(f, "Invalid opcode: {}", value),
            CompileError => write!(f, "Compile error"),
            RuntimeError => write!(f, "Runtime error"),
            IoError(ref err) => err.fmt(f),
        }
    }
}

impl error::Error for Error {}

impl From<io::Error> for Error {
    fn from(err: io::Error) -> Error {
        IoError(err)
    }
}
