package com.gamalocus.sgs.profile.listener.report;

import java.io.Serializable;
import java.math.BigInteger;


public class RawTransactionId implements Serializable
{
	private static final long serialVersionUID = 4858878554015091858L;
	private final long txnId;

    public RawTransactionId(byte [] txnId) 
    {
        this.txnId = (new BigInteger(1, txnId)).longValue();
    }

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (txnId ^ (txnId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final RawTransactionId other = (RawTransactionId) obj;
		if (txnId != other.txnId)
			return false;
		return true;
	}
    
	@Override
	public String toString()
	{
		return "[Txn:"+txnId+"]";
	}
}
