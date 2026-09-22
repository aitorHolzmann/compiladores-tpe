# Makefile - TPE Diseño de Compiladores I
#
#   make test    -> compila el compilador y corre todas las pruebas de tests/
#   make parser  -> regenera Parser.java y ParserVal.java desde gram.y
#                   (necesita byaccj; la profesora no lo usa)
#   make clean   -> borra bin/

FUENTES := $(wildcard src/compilador/*.java src/accion_semantica/*.java)
PRUEBAS := $(filter-out %.java %.md, $(wildcard tests/*))
DESTINO := src/compilador

.PHONY: test parser clean

test:
	@mkdir -p bin
	@javac -encoding UTF-8 -d bin $(FUENTES)
	@for archivo in $(PRUEBAS); do \
		echo ""; \
		echo "================ PRUEBA: $$archivo ================"; \
		cat "$$archivo"; \
		echo ""; \
		echo "---------------- Salida del compilador ----------------"; \
		java -cp bin src.compilador.Parser "$$archivo"; \
	done

parser:
	byaccj -J -v gram.y
	@for f in Parser ParserVal; do \
		{ echo "package src.compilador;"; cat $$f.java; } > $(DESTINO)/$$f.java; \
		rm -f $$f.java; \
	done
	@echo "Parser.java y ParserVal.java actualizados en $(DESTINO)/ (conflictos: ver y.output)"

clean:
	rm -rf bin