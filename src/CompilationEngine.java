

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class CompilationEngine {

    private PrintWriter printWriter;
    private PrintWriter tokenPrintWriter;
    private JackTokenizer tokenizer;

    public CompilationEngine(File inFile, File outFile, File outTokenFile) {

        try {

            tokenizer = new JackTokenizer(inFile);
            printWriter = new PrintWriter(outFile);
            tokenPrintWriter = new PrintWriter(outTokenFile);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


    private void compileType(){

        tokenizer.advance();

        boolean isType = false;

        if (tokenizer.tokenType() == JackTokenizer.KEYWORD && (tokenizer.keyWord() == JackTokenizer.INT || tokenizer.keyWord() == JackTokenizer.CHAR || tokenizer.keyWord() == JackTokenizer.BOOLEAN)){
            printWriter.print("<keyword>" + tokenizer.getCurrentToken() + "</keyword>\n");
            tokenPrintWriter.print("<keyword>" + tokenizer.getCurrentToken() + "</keyword>\n");
            isType = true;
        }

        if (tokenizer.tokenType() == JackTokenizer.IDENTIFIER){
            printWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
            tokenPrintWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
            isType = true;
        }

        if (!isType) error("in|char|boolean|className");
    }


    public void compileClass(){

       
        tokenizer.advance();

        if (tokenizer.tokenType() != JackTokenizer.KEYWORD || tokenizer.keyWord() != JackTokenizer.CLASS){
            error("class");
        }

        printWriter.print("<class>\n");
        tokenPrintWriter.print("<tokens>\n");

        printWriter.print("<keyword>class</keyword>\n");
        tokenPrintWriter.print("<keyword>class</keyword>\n");

        tokenizer.advance();

        if (tokenizer.tokenType() != JackTokenizer.IDENTIFIER){
            error("className");
        }

        printWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
        tokenPrintWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");


        requireSymbol('{');

        compileClassVarDec();
        compileSubroutine();

        requireSymbol('}');

        if (tokenizer.hasMoreTokens()){
            throw new IllegalStateException("Unexpected tokens");
        }

        tokenPrintWriter.print("</tokens>\n");
        printWriter.print("</class>\n");


        printWriter.close();
        tokenPrintWriter.close();

    }


    private void compileClassVarDec(){


        tokenizer.advance();


        if (tokenizer.tokenType() == JackTokenizer.SYMBOL && tokenizer.symbol() == '}'){
            tokenizer.pointerBack();
            return;
        }


        if (tokenizer.tokenType() != JackTokenizer.KEYWORD){
            error("Keywords");
        }

        if (tokenizer.keyWord() == JackTokenizer.CONSTRUCTOR || tokenizer.keyWord() == JackTokenizer.FUNCTION || tokenizer.keyWord() == JackTokenizer.METHOD){
            tokenizer.pointerBack();
            return;
        }

        printWriter.print("<classVarDec>\n");

        if (tokenizer.keyWord() != JackTokenizer.STATIC && tokenizer.keyWord() != JackTokenizer.FIELD){
            error("static or field");
        }

        printWriter.print("<keyword>" + tokenizer.getCurrentToken() + "</keyword>\n");
        tokenPrintWriter.print("<keyword>" + tokenizer.getCurrentToken() + "</keyword>\n");


        compileType();


        boolean varNamesDone = false;

        do {


            tokenizer.advance();
            if (tokenizer.tokenType() != JackTokenizer.IDENTIFIER){
                error("identifier");
            }

            printWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
            tokenPrintWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");

            tokenizer.advance();

            if (tokenizer.tokenType() != JackTokenizer.SYMBOL || (tokenizer.symbol() != ',' && tokenizer.symbol() != ';')){
                error("',' or ';'");
            }

            if (tokenizer.symbol() == ','){

                printWriter.print("<symbol>,</symbol>\n");
                tokenPrintWriter.print("<symbol>,</symbol>\n");

            }else {

                printWriter.print("<symbol>;</symbol>\n");
                tokenPrintWriter.print("<symbol>;</symbol>\n");
                break;
            }


        }while(true);

        printWriter.print("</classVarDec>\n");

        compileClassVarDec();
    }


    private void compileSubroutine(){

        tokenizer.advance();

        if (tokenizer.tokenType() == JackTokenizer.SYMBOL && tokenizer.symbol() == '}'){
            tokenizer.pointerBack();
            return;
        }

        if (tokenizer.tokenType() != JackTokenizer.KEYWORD || (tokenizer.keyWord() != JackTokenizer.CONSTRUCTOR && tokenizer.keyWord() != JackTokenizer.FUNCTION && tokenizer.keyWord() != JackTokenizer.METHOD)){
            error("constructor|function|method");
        }

        printWriter.print("<subroutineDec>\n");

        printWriter.print("<keyword>" + tokenizer.getCurrentToken() + "</keyword>\n");
        tokenPrintWriter.print("<keyword>" + tokenizer.getCurrentToken() + "</keyword>\n");


        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.KEYWORD && tokenizer.keyWord() == JackTokenizer.VOID){
            printWriter.print("<keyword>void</keyword>\n");
            tokenPrintWriter.print("<keyword>void</keyword>\n");
        }else {
            tokenizer.pointerBack();
            compileType();
        }

        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.IDENTIFIER){
            error("subroutineName");
        }

        printWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
        tokenPrintWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");

        requireSymbol('(');

        printWriter.print("<parameterList>\n");
        compileParameterList();
        printWriter.print("</parameterList>\n");

        requireSymbol(')');

        compileSubroutineBody();

        printWriter.print("</subroutineDec>\n");

        compileSubroutine();

    }


    private void compileSubroutineBody(){
        printWriter.print("<subroutineBody>\n");

        requireSymbol('{');

        compileVarDec();
     
        printWriter.print("<statements>\n");
        compileStatement();
        printWriter.print("</statements>\n");
     
        requireSymbol('}');
        printWriter.print("</subroutineBody>\n");
    }

    
    private void compileStatement(){

        
        tokenizer.advance();

        
        if (tokenizer.tokenType() == JackTokenizer.SYMBOL && tokenizer.symbol() == '}'){
            tokenizer.pointerBack();
            return;
        }

        
        if (tokenizer.tokenType() != JackTokenizer.KEYWORD){
            error("keyword");
        }else {
            switch (tokenizer.keyWord()){
                case JackTokenizer.LET:compileLet();break;
                case JackTokenizer.IF:compileIf();break;
                case JackTokenizer.WHILE:compilesWhile();break;
                case JackTokenizer.DO:compileDo();break;
                case JackTokenizer.RETURN:compileReturn();break;
                default:error("'let'|'if'|'while'|'do'|'return'");
            }
        }

        compileStatement();
    }

    private void compileParameterList(){

       
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.SYMBOL && tokenizer.symbol() == ')'){
            tokenizer.pointerBack();
            return;
        }

        tokenizer.pointerBack();
        do {
           
            compileType();

            tokenizer.advance();
            if (tokenizer.tokenType() != JackTokenizer.IDENTIFIER){
                error("identifier");
            }
             printWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
            tokenPrintWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");

           
            tokenizer.advance();
            if (tokenizer.tokenType() != JackTokenizer.SYMBOL || (tokenizer.symbol() != ',' && tokenizer.symbol() != ')')){
                error("',' or ')'");
            }

            if (tokenizer.symbol() == ','){
                printWriter.print("<symbol>,</symbol>\n");
                tokenPrintWriter.print("<symbol>,</symbol>\n");
            }else {
                tokenizer.pointerBack();
                break;
            }

        }while(true);

    }

  
    private void compileVarDec(){

       

        tokenizer.advance();
     
        if (tokenizer.tokenType() != JackTokenizer.KEYWORD || tokenizer.keyWord() != JackTokenizer.VAR){
            tokenizer.pointerBack();
            return;
        }

        printWriter.print("<varDec>\n");

        printWriter.print("<keyword>var</keyword>\n");
        tokenPrintWriter.print("<keyword>var</keyword>\n");

        compileType();
      
        boolean varNamesDone = false;

        do {

            
            tokenizer.advance();

            if (tokenizer.tokenType() != JackTokenizer.IDENTIFIER){
                error("identifier");
            }

            printWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
            tokenPrintWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");

        
            tokenizer.advance();

            if (tokenizer.tokenType() != JackTokenizer.SYMBOL || (tokenizer.symbol() != ',' && tokenizer.symbol() != ';')){
                error("',' or ';'");
            }

            if (tokenizer.symbol() == ','){

                printWriter.print("<symbol>,</symbol>\n");
                tokenPrintWriter.print("<symbol>,</symbol>\n");

            }else {

                printWriter.print("<symbol>;</symbol>\n");
                tokenPrintWriter.print("<symbol>;</symbol>\n");
                break;
            }


        }while(true);

        printWriter.print("</varDec>\n");

        compileVarDec();

    }

   
    private void compileDo(){
        printWriter.print("<doStatement>\n");

        printWriter.print("<keyword>do</keyword>\n");
        tokenPrintWriter.print("<keyword>do</keyword>\n");
    
        compileSubroutineCall();
     
        requireSymbol(';');

        printWriter.print("</doStatement>\n");
    }

 
    private void compileLet(){

        printWriter.print("<letStatement>\n");

        printWriter.print("<keyword>let</keyword>\n");
        tokenPrintWriter.print("<keyword>let</keyword>\n");

        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.IDENTIFIER){
            error("varName");
        }

        printWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
        tokenPrintWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");

        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.SYMBOL || (tokenizer.symbol() != '[' && tokenizer.symbol() != '=')){
            error("'['|'='");
        }

        boolean expExist = false;

        if (tokenizer.symbol() == '['){

            expExist = true;

            printWriter.print("<symbol>[</symbol>\n");
            tokenPrintWriter.print("<symbol>[</symbol>\n");

            compileExpression();

          
            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokenizer.SYMBOL && tokenizer.symbol() == ']'){
                printWriter.print("<symbol>]</symbol>\n");
                tokenPrintWriter.print("<symbol>]</symbol>\n");
            }else {
                error("']'");
            }
        }

        if (expExist) tokenizer.advance();

        printWriter.print("<symbol>=</symbol>\n");
        tokenPrintWriter.print("<symbol>=</symbol>\n");

        compileExpression();

       
        requireSymbol(';');

        printWriter.print("</letStatement>\n");
    }

    private void compilesWhile(){
        printWriter.print("<whileStatement>\n");

        printWriter.print("<keyword>while</keyword>\n");
        tokenPrintWriter.print("<keyword>while</keyword>\n");
        
        requireSymbol('(');
        
        compileExpression();
        
        requireSymbol(')');
        
        requireSymbol('{');
       
        printWriter.print("<statements>\n");
        compileStatement();
        printWriter.print("</statements>\n");
       
        requireSymbol('}');

        printWriter.print("</whileStatement>\n");
    }

   
    private void compileReturn(){
        printWriter.print("<returnStatement>\n");

        printWriter.print("<keyword>return</keyword>\n");
        tokenPrintWriter.print("<keyword>return</keyword>\n");

  
        tokenizer.advance();
       
        if (tokenizer.tokenType() == JackTokenizer.SYMBOL && tokenizer.symbol() == ';'){
            printWriter.print("<symbol>;</symbol>\n");
            tokenPrintWriter.print("<symbol>;</symbol>\n");
            printWriter.print("</returnStatement>\n");
            return;
        }

        tokenizer.pointerBack();
       
        compileExpression();
        
        requireSymbol(';');

        printWriter.print("</returnStatement>\n");
    }

    
    private void compileIf(){
        printWriter.print("<ifStatement>\n");

        printWriter.print("<keyword>if</keyword>\n");
        tokenPrintWriter.print("<keyword>if</keyword>\n");
      
        requireSymbol('(');
        
        compileExpression();
       
        requireSymbol(')');
      
        requireSymbol('{');
      
        printWriter.print("<statements>\n");
        compileStatement();
        printWriter.print("</statements>\n");
        
        requireSymbol('}');

     
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.KEYWORD && tokenizer.keyWord() == JackTokenizer.ELSE){
            printWriter.print("<keyword>else</keyword>\n");
            tokenPrintWriter.print("<keyword>else</keyword>\n");
         
            requireSymbol('{');
        
            printWriter.print("<statements>\n");
            compileStatement();
            printWriter.print("</statements>\n");
          
            requireSymbol('}');
        }else {
            tokenizer.pointerBack();
        }

        printWriter.print("</ifStatement>\n");

    }

   
    private void compileTerm(){

        printWriter.print("<term>\n");

        tokenizer.advance();
        
        if (tokenizer.tokenType() == JackTokenizer.IDENTIFIER){
            
            String tempId = tokenizer.identifier();

            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokenizer.SYMBOL && tokenizer.symbol() == '['){
                printWriter.print("<identifier>" + tempId + "</identifier>\n");
                tokenPrintWriter.print("<identifier>" + tempId + "</identifier>\n");
                
                printWriter.print("<symbol>[</symbol>\n");
                tokenPrintWriter.print("<symbol>[</symbol>\n");
                
                compileExpression();
                
                requireSymbol(']');
            }else if (tokenizer.tokenType() == JackTokenizer.SYMBOL && (tokenizer.symbol() == '(' || tokenizer.symbol() == '.')){
              
                tokenizer.pointerBack();tokenizer.pointerBack();
                compileSubroutineCall();
            }else {
                printWriter.print("<identifier>" + tempId + "</identifier>\n");
                tokenPrintWriter.print("<identifier>" + tempId + "</identifier>\n");
                
                tokenizer.pointerBack();
            }

        }else{
            
            if (tokenizer.tokenType() == JackTokenizer.INT_CONST){
                printWriter.print("<integerConstant>" + tokenizer.intVal() + "</integerConstant>\n");
                tokenPrintWriter.print("<integerConstant>" + tokenizer.intVal() + "</integerConstant>\n");
            }else if (tokenizer.tokenType() == JackTokenizer.STRING_CONST){
                printWriter.print("<stringConstant>" + tokenizer.stringVal() + "</stringConstant>\n");
                tokenPrintWriter.print("<stringConstant>" + tokenizer.stringVal() + "</stringConstant>\n");
            }else if(tokenizer.tokenType() == JackTokenizer.KEYWORD &&
                            (tokenizer.keyWord() == JackTokenizer.TRUE ||
                            tokenizer.keyWord() == JackTokenizer.FALSE ||
                            tokenizer.keyWord() == JackTokenizer.NULL ||
                            tokenizer.keyWord() == JackTokenizer.THIS)){
                    printWriter.print("<keyword>" + tokenizer.getCurrentToken() + "</keyword>\n");
                    tokenPrintWriter.print("<keyword>" + tokenizer.getCurrentToken() + "</keyword>\n");
            }else if (tokenizer.tokenType() == JackTokenizer.SYMBOL && tokenizer.symbol() == '('){
                printWriter.print("<symbol>(</symbol>\n");
                tokenPrintWriter.print("<symbol>(</symbol>\n");
                
                compileExpression();
               
                requireSymbol(')');
            }else if (tokenizer.tokenType() == JackTokenizer.SYMBOL && (tokenizer.symbol() == '-' || tokenizer.symbol() == '~')){
                printWriter.print("<symbol>" + tokenizer.symbol() + "</symbol>\n");
                tokenPrintWriter.print("<symbol>" + tokenizer.symbol() + "</symbol>\n");
               
                compileTerm();
            }else {
                error("integerConstant|stringConstant|keywordConstant|'(' expression ')'|unaryOp term");
            }
        }

        printWriter.print("</term>\n");
    }

    
    private void compileSubroutineCall(){

        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.IDENTIFIER){
            error("identifier");
        }

        printWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
        tokenPrintWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");

        tokenizer.advance();

        if (tokenizer.tokenType() == JackTokenizer.SYMBOL && tokenizer.symbol() == '('){
            printWriter.print("<symbol>(</symbol>\n");
            tokenPrintWriter.print("<symbol>(</symbol>\n");
            printWriter.print("<expressionList>\n");
            compileExpressionList();
            printWriter.print("</expressionList>\n");
        
            requireSymbol(')');
        }

        else if (tokenizer.tokenType() == JackTokenizer.SYMBOL && tokenizer.symbol() == '.'){
            
            printWriter.print("<symbol>.</symbol>\n");
            tokenPrintWriter.print("<symbol>.</symbol>\n");
            
            tokenizer.advance();
            if (tokenizer.tokenType() != JackTokenizer.IDENTIFIER){
                error("identifier");
            }
            printWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
            tokenPrintWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
           
            requireSymbol('(');
            
            printWriter.print("<expressionList>\n");
            compileExpressionList();
            printWriter.print("</expressionList>\n");
           
            requireSymbol(')');
        }else {
            error("'('|'.'");
        }
    }

    
    private void compileExpression(){
        printWriter.print("<expression>\n");

       
        compileTerm();
        
        do {
            tokenizer.advance();
           
            if (tokenizer.tokenType() == JackTokenizer.SYMBOL && tokenizer.isOp()){
                if (tokenizer.symbol() == '>'){
                    printWriter.print("<symbol>&gt;</symbol>\n");
                    tokenPrintWriter.print("<symbol>&gt;</symbol>\n");
                }else if (tokenizer.symbol() == '<'){
                    printWriter.print("<symbol>&lt;</symbol>\n");
                    tokenPrintWriter.print("<symbol>&lt;</symbol>\n");
                }else if (tokenizer.symbol() == '&') {
                    printWriter.print("<symbol>&amp;</symbol>\n");
                    tokenPrintWriter.print("<symbol>&amp;</symbol>\n");
                }else {
                    printWriter.print("<symbol>" + tokenizer.symbol() + "</symbol>\n");
                    tokenPrintWriter.print("<symbol>" + tokenizer.symbol() + "</symbol>\n");
                }
               
                compileTerm();
            }else {
                tokenizer.pointerBack();
                break;
            }

        }while (true);

        printWriter.print("</expression>\n");
    }

   
    private void compileExpressionList(){
        tokenizer.advance();
        
        if (tokenizer.tokenType() == JackTokenizer.SYMBOL && tokenizer.symbol() == ')'){
            tokenizer.pointerBack();
        }else {

            tokenizer.pointerBack();
            
            compileExpression();
          
            do {
                tokenizer.advance();
                if (tokenizer.tokenType() == JackTokenizer.SYMBOL && tokenizer.symbol() == ','){
                    printWriter.print("<symbol>,</symbol>\n");
                    tokenPrintWriter.print("<symbol>,</symbol>\n");
                   
                    compileExpression();
                }else {
                    tokenizer.pointerBack();
                    break;
                }

            }while (true);

        }
    }

    
    private void error(String val){
        throw new IllegalStateException("Expected token missing : " + val + " Current token:" + tokenizer.getCurrentToken());
    }

    
    private void requireSymbol(char symbol){
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.SYMBOL && tokenizer.symbol() == symbol){
            printWriter.print("<symbol>" + symbol + "</symbol>\n");
            tokenPrintWriter.print("<symbol>" + symbol + "</symbol>\n");
        }else {
            error("'" + symbol + "'");
        }
    }
}
