/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

/**
 *
 * @author Maikel Maciel Rönnau
 */
public class UnknownCommandException extends Exception {
    
    public UnknownCommandException(String message) {
        super(message);
    }
}
