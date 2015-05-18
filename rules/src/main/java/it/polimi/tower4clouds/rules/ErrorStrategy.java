/**
 * Copyright (C) 2014 Politecnico di Milano (marco.miglierina@polimi.it)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.polimi.tower4clouds.rules;

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.FailedPredicateException;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.NotNull;

class ErrorStrategy extends DefaultErrorStrategy {
	
	//OVERRIDE METHODS TO AVOID USELESS (ACCORDING TO OUR PURPOSE) BEHAVIOUR OF ANTLR4
	/** Instead of recovering from exception e, rethrow it wrapped
     *  in a generic RuntimeException so it is not caught by the
     *  rule function catches.  Exception e is the "cause" of the
     *  RuntimeException.
     */
	@Override
	public void recover(Parser recognizer, RecognitionException e) {
		throw new RuntimeException(e); 
	}
	
	/** Make sure we don't attempt to recover inline; if the parser
     *  successfully recovers, it won't throw an exception.
     */
	@Override
	public Token recoverInline(Parser recognizer) throws RecognitionException {
		throw new RuntimeException(new InputMismatchException(recognizer)); 
	}
	
	/** Make sure we don't attempt to recover from problems in subrules. */
	@Override
	public void sync(Parser recognizer) { }
	
	
	@Override
	public void reportError(Parser recognizer, RecognitionException e) { 
		
		// if we've already reported an error and have not matched a token
		// yet successfully, don't report any errors.
		if (inErrorRecoveryMode(recognizer)) {
//			System.err.print("[SPURIOUS] ");
			return; // don't report spurious errors
		}
		beginErrorCondition(recognizer);
		if ( e instanceof NoViableAltException ) {
			reportNoViableAlternative(recognizer, (NoViableAltException) e);
		}
		else if ( e instanceof InputMismatchException ) {
			reportInputMismatch(recognizer, (InputMismatchException)e);
		}
		else if ( e instanceof FailedPredicateException ) {
			reportFailedPredicate(recognizer, (FailedPredicateException)e);
		}
		else {
			System.err.println("unknown recognition error type: "+e.getClass().getName());
			recognizer.notifyErrorListeners(e.getOffendingToken(), e.getMessage(), e);
		}
		//Listener listener = new Listener();
		//listener.syntaxError(e.getRecognizer(), e.getOffendingToken(), e.getOffendingToken().getLine(), e.getOffendingToken().getCharPositionInLine(), e.getMessage(), e);
	}
	

	/**
	 * This is called by {@link #reportError} when the exception is a
	 * {@link NoViableAltException}.
	 *
	 * @see #reportError
	 *
	 * @param recognizer the parser instance
	 * @param e the recognition exception
	 */
	@Override
	protected void reportNoViableAlternative(@NotNull Parser recognizer,
											 @NotNull NoViableAltException e)
	{
		/*TokenStream tokens = recognizer.getInputStream();
		String input;
		if ( tokens!=null ) {
			if ( e.getStartToken().getType()==Token.EOF ) input = "<EOF>";
			else input = tokens.getText(e.getStartToken(), e.getOffendingToken());
		}
		else {
			input = "<unknown input>";
		}
		String msg = "no viable alternative at input "+escapeWSAndQuote(input);
		recognizer.notifyErrorListeners(e.getOffendingToken(), msg, e);*/
	}

	/**
	 * This is called by {@link #reportError} when the exception is an
	 * {@link InputMismatchException}.
	 *
	 * @see #reportError
	 *
	 * @param recognizer the parser instance
	 * @param e the recognition exception
	 */
	@Override
	protected void reportInputMismatch(@NotNull Parser recognizer,
									   @NotNull InputMismatchException e)
	{
		/*String msg = "mismatched input "+getTokenErrorDisplay(e.getOffendingToken())+
		" expecting "+e.getExpectedTokens().toString(recognizer.getTokenNames());
		recognizer.notifyErrorListeners(e.getOffendingToken(), msg, e);*/
	}

	/**
	 * This is called by {@link #reportError} when the exception is a
	 * {@link FailedPredicateException}.
	 *
	 * @see #reportError
	 *
	 * @param recognizer the parser instance
	 * @param e the recognition exception
	 */
	@Override
	protected void reportFailedPredicate(@NotNull Parser recognizer,
										 @NotNull FailedPredicateException e)
	{
		/*String ruleName = recognizer.getRuleNames()[recognizer.getContext().getRuleIndex()];
		String msg = "rule "+ruleName+" "+e.getMessage();
		recognizer.notifyErrorListeners(e.getOffendingToken(), msg, e);*/
	}
}