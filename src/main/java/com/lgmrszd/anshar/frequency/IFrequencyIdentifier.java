package com.lgmrszd.anshar.frequency;

/*
 * Also supports equality comparison to determine network presence.
 */
public sealed interface IFrequencyIdentifier permits NullFrequencyIdentifier, HashFrequencyIdentifier {
    public boolean isValid();
}
