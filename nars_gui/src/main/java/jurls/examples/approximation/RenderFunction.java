/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.examples.approximation;

import jurls.core.approximation.ParameterizedFunction;

import java.awt.Color;

/**
 *
 * @author thorsten
 */
@Deprecated public interface RenderFunction extends ParameterizedFunction {

    public double compute(double x);

    public Color getColor();
}