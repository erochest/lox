use crate::error::Result;
use crate::scanner::Scanner;

pub fn compile<S: AsRef<str>>(source: S) -> Result<()> {
    let scanner = Scanner::new(source.as_ref().to_string());
    Ok(())
}
