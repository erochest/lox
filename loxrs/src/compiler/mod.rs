use crate::chunk::Chunk;
use crate::error::{Error, Result};
use crate::scanner::{Scanner, Token, TokenType};

struct Compiler {
    chunk: Option<Chunk>,
    current: Option<Token>,
    previous: Option<Token>,
    has_errors: bool,
}

pub fn compile<S: AsRef<str>>(source: S) -> Result<Chunk> {
    let source = source.as_ref().to_string();
    let mut scanner = Scanner::new(source);
    let mut compiler = Compiler::new();

    compiler.advance(&mut scanner);
    compiler.expression()?;
    compiler.consume(TokenType::EOF, &mut scanner, "Expect end of expression");

    if compiler.has_errors {
        Err(Error::CompileError)
    } else {
        compiler.chunk.take().ok_or(Error::MissingChunkError)
    }
}

impl Compiler {
    fn new() -> Self {
        Self {
            chunk: None,
            current: None,
            previous: None,
            has_errors: false,
        }
    }

    fn advance(&mut self, scanner: &mut Scanner) {
        self.previous = self.current.take();

        let (current, errors) = scanner.scan_to_next();
        self.has_errors = !errors.is_empty();
        if errors.first().is_some() {
            let text = current
                .as_ref()
                .map(|t| t.text(&scanner.input))
                .unwrap_or("Missing token");
            self.error_at_current(scanner, text);
        }

        self.current = current;
    }

    fn expression(&mut self) -> Result<()> {
        todo!()
    }

    fn consume(&mut self, ty: TokenType, scanner: &mut Scanner, message: &str) {
        if self.current.as_ref().map(|t| t.ty) == Some(ty) {
            self.advance(scanner);
            return;
        }
        self.error_at_current(scanner, message)
    }

    fn error_at_current(&self, scanner: &Scanner, message: &str) {
        self.error_at(&self.previous, scanner, message);
    }

    fn error_at(&self, previous: &Option<Token>, scanner: &Scanner, message: &str) {
        let previous = previous.as_ref();
        let line = previous.map(|token| token.line).unwrap_or_default();
        eprint!("[line {}] Error", line);

        match previous.map(|token| token.ty) {
            Some(TokenType::EOF) => eprint!(" at end"),
            Some(TokenType::Error) => {}
            Some(_) => {
                // TODO: seems like this is getting called twice.
                let token_text = previous.map(|t| t.text(&scanner.input));
                eprint!(" at {:?}", token_text.unwrap_or_default());
            }
            None => {}
        }

        eprintln!(": {}", message);
    }
}
