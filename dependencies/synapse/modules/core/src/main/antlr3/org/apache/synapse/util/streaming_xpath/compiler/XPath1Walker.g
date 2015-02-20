        tree grammar XPath1Walker;

        options {
        backtrack=true;
        tokenVocab=XPath1;
        ASTLabelType=CommonTree;
        }

        tokens {
        QNAME; STRING; NUMBER; VARREF;
        OREXPR; ANDEXPR; UNIONEXPR; EQUEXPRESSION;
        PREDICATE; FUNCALL;
        NAMED_AXIS_STEP; SIMPLE_AXIS_STEP; ABBREVIATED_AXIS_STEP;
        NAME_AXIS; ATTRIBUTE_AXIS;
        EXPRLIST;
        ANY_NODE; ANY_NAMESPACED_NODE;
        LOCATION_PATH;
        QNAME_PREDICATE;
        }
        @header {
        package org.apache.synapse.util.streaming_xpath.compiler;

        import org.apache.synapse.util.streaming_xpath.custom.*;
        import org.apache.synapse.util.streaming_xpath.compiler.exception.*;
        import javax.xml.namespace.QName;
        
        }
        // ---------------- Streaming Parser Rules ---------------- //
        @members{
        boolean firstXPATH=true;
        String xpath=null;
        StreamingParser localParser=new StreamingParser();

        }

        xpath returns [StreamingParser xpathParser]
        : locationPath {$xpathParser=localParser;}
        | ^(SYNAPSE_SPECIFIC SynapseSpecific locationPath)
        ;

        locationPath 
        :^(LOCATION_PATH SynapseSpecificBody relativeLocationPath { throw new StreamingXPATHCompilerException();})
        |^(LOCATION_PATH SynapseSpecific { throw new StreamingXPATHCompilerException();} relativeLocationPath)
        |^(LOCATION_PATH SynapseSpecificBody{
         localParser.GetChild_GetChildrenByName("Body","soapenv");
         firstXPATH=false;
        }absoluteLocationPath)
        |^(LOCATION_PATH SynapseSpecific { throw new StreamingXPATHCompilerException();} absoluteLocationPath)
        |^(LOCATION_PATH relativeLocationPath  )
        |^(LOCATION_PATH absoluteLocationPath)
        |^(LOCATION_PATH Name{ throw new StreamingXPATHCompilerException();})
        ;

        absoluteLocationPath 
        :^(SingleAxisStep  relativeLocationPath? )
        |^(RecursiveAxisStep relativeLocationPath )
        ;

        relativeLocationPath 
        :^(SingleAxisStep  step relativeLocationPath)
        |^(RecursiveAxisStep  step  relativeLocationPath)
        |step
        ;

        step 
        :^(QNAME_PREDICATE namedAxisStep (predicate*)?)
        |^(QNAME_PREDICATE abbreviatedAxisStep (predicate*)?)
        ;
           
        abbreviatedAxisStep 
        : ^(ABBREVIATED_AXIS_STEP '.' {localParser.GetChild_GetCurrent();})
        | ^(ABBREVIATED_AXIS_STEP '..' {localParser.GetChild_FirstChild();})
        ;

        namedAxisStep 
        :^(ATTRIBUTE_AXIS AbbreviatedAxisSpecifier nodeTest {localParser.GetChild_GetAttribute($nodeTest.qname.getLocalPart(),$nodeTest.qname.getNamespaceURI());})
        |^(NAMED_AXIS_STEP axisSpecifier? nodeTest { 
                        boolean absolute=true;
                        String name="";
                        if($nodeTest.qname.getNamespaceURI()!=null &&$nodeTest.qname.getNamespaceURI()!=""){
                               name=$nodeTest.qname.getNamespaceURI()+":"+$nodeTest.qname.getLocalPart();
                        }
                        else{
                              name=$nodeTest.qname.getLocalPart();
                        }

                        if(xpath.charAt(xpath.indexOf(name)-1)=='/'){
                            if(xpath.indexOf(name)-2>=0 && xpath.charAt(xpath.indexOf(name)-2)=='/'){
                                absolute=false;
                            }
                        }
                        if(firstXPATH&&(absolute)){
                            localParser.GetChild_GetCurrentMatch($nodeTest.qname.getLocalPart(),$nodeTest.qname.getNamespaceURI());
                            firstXPATH=false;
                        }
                        else{
                            firstXPATH=false;
                            if(!absolute){
                                localParser.GetChild_GetChildrenByNameRelative($nodeTest.qname.getLocalPart(),$nodeTest.qname.getNamespaceURI());
                            }
                            else{
                                localParser.GetChild_GetChildrenByName($nodeTest.qname.getLocalPart(),$nodeTest.qname.getNamespaceURI());
                            }

                        }
        })
        ;

        axisSpecifier
        :^(NAME_AXIS AxisName{throw new StreamingXPATHCompilerException();})
        ;   

        nodeTest returns [QName qname]
        : nameTest  {$qname=$nameTest.qname;}
        | ^(ANY_NODE NodeType{throw new StreamingXPATHCompilerException();})
        | ProcessingInstruction{throw new StreamingXPATHCompilerException();} LeftParenthesis StringLiteral RightParenthesis
        ;

        nameTest returns [QName qname]
        : ^(ANY_NODE{throw new StreamingXPATHCompilerException();} ANY_NODE )
        |^(ANY_NAMESPACED_NODE Name{throw new StreamingXPATHCompilerException();})
        | qName {$qname=$qName.qname;}
        ;

        predicate
        : ^(PREDICATE predicateExpr{throw new StreamingXPATHCompilerException();})
        |^(PREDICATE '1')
        ;

        predicateExpr : expr;

        functionCall
        : ^(FUNCALL qName expressionList?)
        ;

        expressionList
        : ^(EXPRLIST expr+)
        ;

        pathExpr
        : locationPath
        | filterExpr (simpleAxisStep  relativeLocationPath)?
        ;
        
        simpleAxisStep
        :^(SIMPLE_AXIS_STEP SingleAxisStep)
        |^(SIMPLE_AXIS_STEP RecursiveAxisStep)
        ;
        
        filterExpr
        : primaryExpr predicate?;

        primaryExpr
        : LeftParenthesis expr RightParenthesis
        | literal
        | functionCall
        ;

        expr
        : orExpr
        ;

        orExpr
        :^(OREXPR andExpr+)
        ;

        andExpr
        : ^(ANDEXPR equalityExpr+)
        ;

        equalityExpr
        : ^(EQUEXPRESSION relationalExpr (EqualtyOp relationalExpr)?)
        ;

        relationalExpr
        : unionExpr
        ;

        unionExpr
        :^(UNIONEXPR pathExpr+)
        ;

        literal
        : ^(STRING StringLiteral)
        |^(NUMBER numericLiteral)
        |^(VARREF VariableReference)
        ;

        numericLiteral
        : IntegerLiteral
        |DecimalLiteral
        |DoubleLiteral;
        
        qName returns [QName qname]
        
        @init{
        String namespace=""; 
        String localname="";
        }
        : ^(NameSpacedQNAME  
        a=Name b=Name
        {
        namespace=$a.text;
        localname=$b.text;
        $qname=new QName(namespace,localname);
        }  ) 
        | ^(QNAME c=Name
        {
        localname=$c.text;
        $qname=new QName("",localname);
        } )
        ;