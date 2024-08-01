use crate::error::Result;
use crate::scanner::Scanner;

pub fn compile<S: AsRef<str>>(source: S) -> Result<()> {
    let mut scanner = Scanner::new(source.as_ref().to_string());

    let mut line: usize = 0;
    loop {
        let token = scanner.scan_token()?;

        if token.line != line {
            line = token.line;
            println!("{:4} ", line);
        } else {
            print!("   | ");
        }

        println!("{:#?}. {:#?}", token.ty, token.token);

        if token.ty == crate::scanner::TokenType::EOF {
            break;
        }
    }

    Ok(())
}
