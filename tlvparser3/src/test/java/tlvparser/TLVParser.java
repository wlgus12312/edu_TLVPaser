package tlvparser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubivelox.gaia.GaiaException;
import com.ubivelox.gaia.util.GaiaUtils;

import exception.UbiveloxException;
import lombok.Data;

@Data
public class TLVParser
{

    public enum ValueType
    {
        PRIMITIVE,
        CONSTRUCTED;
    }

    private static final Logger logger = LoggerFactory.getLogger(TlvParserTest.class);

    static class TLVResultNBytePosition
    {
        String tlvResult;
        int    byteArrayPosition;





        public TLVResultNBytePosition(final String tlvResult, final int byteArrayPosition)
        {
            this.tlvResult = tlvResult;
            this.byteArrayPosition = byteArrayPosition;
        }
    }





    public static String parse(final String hexStringOrg) throws UbiveloxException, GaiaException
    {

        byte[] byteArrayOrg = GaiaUtils.convertHexaStringToByteArray(hexStringOrg);

        return parse1(byteArrayOrg, 0, 0);
    }





    private static String parse1(final byte[] byteArrayOrg, final int dept, final int byteArrayPosition2) throws UbiveloxException, GaiaException
    {
        // GaiaUtils.checkHexaString(hexStringOrg);
        // String hexString = hexStringOrg;

        int hexStringIndex = 0;
        TLVResultNBytePosition parseOne;
        int tlvIndex = 0;
        byte[] byteArray = byteArrayOrg;

        int byteArrayPosition;

        String result = "";
        do
        {
            int deptint = dept;
            String tapDept = "";
            String outPut = "";
            {

                ValueType valueType = ValueType.PRIMITIVE;
                byteArrayPosition = tlvIndex + byteArrayPosition2;
                int tSize = 0;
                int lSize = 0;
                int vSize = 0;

                String tagString = "";
                String lengthString = "";
                String valueString = "";

                System.out.println("qq" + byteArrayPosition);

                // primitive와 constructed 구분
                if ( ((byteArray[byteArrayPosition]) & 0b0010_0000) == 0b0010_0000 )
                {
                    valueType = ValueType.CONSTRUCTED;

                }
                else
                {
                    valueType = ValueType.PRIMITIVE;
                }

                tSize = getTagSize(byteArray, byteArrayPosition);
                // byteArrayPosition += tSize / 2;

                for ( int i = byteArrayPosition; i < byteArrayPosition + tSize; i++ )
                {
                    tagString += GaiaUtils.convertByteToHexaString(byteArray[i]);
                }
                System.out.println(tagString);

                byteArrayPosition += tSize;

                if ( byteArray.length == byteArrayPosition )

                {
                    throw new UbiveloxException("Length Range is not exist");

                }
                else
                {

                    lSize = getLengthSize(byteArray, byteArrayPosition);
                    // 렝스의 길이 조회
                    String temp = GaiaUtils.convertByteToHexaString(byteArray[byteArrayPosition]);
                    // byteArrayPosition = ((tSize + lSize) / 2) - 1 + tlvIndex;

                    for ( int i = 0; i < deptint; i++ )
                    {
                        tapDept += "\t";
                    }

                    if ( lSize == 0 )
                    {
                        lengthString = "00";

                    }
                    else
                    {

                        for ( int i = byteArrayPosition; i < byteArrayPosition + tSize + lSize - 1; i++ )
                        {
                            lengthString += GaiaUtils.convertByteToHexaString(byteArray[i]);
                        }
                        System.out.println(lengthString);
                        // outPut += tapDept + hexString.substring(0, tSize) + "\t" + hexString.substring(tSize, tSize + lSize);
                    }
                    byteArrayPosition = ((tSize + lSize)) - 1 + tlvIndex;

                    outPut += tapDept + tagString + "\t" + lengthString;
                }

                // length가 0이면 val없이 output
                // if (byteArray[byteArrayPosition] == 0) {
                if ( lSize == 0 )
                {

                    parseOne = new TLVResultNBytePosition(outPut, byteArrayPosition);
                    // tlvIndex += (tSize + lSize + vSize) / 2;
                    tlvIndex += (tSize + lSize + vSize);

                }
                else

                {
                    // hexString.length() > byteArray.length
                    if ( (tSize + lSize) == byteArray.length + 1 )
                    {
                        throw new UbiveloxException("Value Range is not exist");
                    }
                    // vSize = byteArray[byteArrayPosition] * 2;
                    vSize = byteArray[byteArrayPosition];
                    // hexString.length() > byteArray.length
                    if ( (tSize + lSize + vSize) > byteArray.length + 1 )
                    {
                        throw new UbiveloxException("Value Range is not enough");
                    }

                    if ( valueType == ValueType.PRIMITIVE )
                    {

                        for ( int i = byteArrayPosition + 1; i < byteArrayPosition + (tSize + lSize + vSize) - 1; i++ )
                        {
                            valueString += GaiaUtils.convertByteToHexaString(byteArray[i]);
                            System.out.println(valueString);
                        }

                        // outPut += "\t" + hexString.substring((tSize + lSize), (tSize + lSize + vSize));
                        outPut += "\t" + valueString;
                    }
                    else
                    {
                        deptint++;
                        // valueValue = byteToHexaString(byteBuffer.get(byteArray, (tSize + lSize), (tSize + lSize + vSize)));
                        // outPut += "\n" + parse1(hexString.substring((tSize + lSize), (tSize + lSize + vSize)), deptint);
                        outPut += "2" + parse1(byteArray, deptint, byteArrayPosition);
                    }
                    // parseOne = new TLVResultNBytePosition(outPut, byteArrayPosition + (vSize / 2));
                    System.out.println(outPut);
                    parseOne = new TLVResultNBytePosition(outPut, (byteArrayPosition + (vSize)));
                    // tlvIndex += (tSize + lSize + vSize) / 2;
                    tlvIndex += (tSize + lSize + vSize);

                }

            }
            result += (hexStringIndex == 0 ? "" : "\n") + parseOne.tlvResult;
            System.out.println("result = " + result);
            hexStringIndex = parseOne.byteArrayPosition * 2 + 2;
            byteArrayPosition = parseOne.byteArrayPosition;
            // hexString = hexStringOrg.substring(hexStringIndex);
            // } while (!hexString.isEmpty());

        }
        while ( tlvIndex < byteArray.length );
        return result;
    }





    static int getTagSize(final byte[] byteArray, int byteArrPos) throws UbiveloxException, GaiaException
    {
        // checkNLO(byteArray, byteArray == null ? 0 : byteArray.length, "Tag Byte
        // Array");
        GaiaUtils.checkNullOrEmpty(byteArray);

        // int tSize = 2;
        int tSize = 1;

        if ( ((byteArray[byteArrPos]) & 0b0001_1111) == 0b0001_1111 )
        {
            tSize += 1;
            for ( int i = byteArrPos + 1; (byteArray[i] & 0b1000_0000) == 0b1000_0000; i++ )
            {
                tSize += 1;
                if ( tSize >= 6 )
                {
                    throw new UbiveloxException("Tag Range Overflow");
                }
            }
        }

        // byteArrPos = tSize / 2;
        byteArrPos = tSize;
        return tSize;
    }





    static int getLengthSize(final byte[] byteArray, final int byteArrPos) throws UbiveloxException, GaiaException
    {
        // checkNLO(byteArr, byteArr == null ? 0 : byteArr.length, "Length Byte Array");
        GaiaUtils.checkNullOrEmpty(byteArray);

        int lSize = 1;

        if ( (byteArray[byteArrPos] & 0xff) > 0x81 )
        {
            throw new UbiveloxException("Length Range Overflow");
        }
        else if ( (byteArray[byteArrPos] & 0xff) == 0x81 )
        {
            logger.info("0x81 들어감");
            lSize += 1;

            if ( byteArrPos + 1 >= byteArray.length )
            {
                throw new UbiveloxException("Length is not enough");
            }
        }

        return lSize;
    }





    public static TLVResultNBytePosition parseOneTLV(final String hexString) throws UbiveloxException, GaiaException
    {

        ValueType valueType = ValueType.PRIMITIVE;
        String outPut = "";

        int tSize = 0;
        int lSize = 0;
        int vSize = 0;
        int byteArrayPosition = 0;

        // checkNLO(hexString, hexString == null ? 0 : hexString.length(),
        // "HexaString");

        byte[] byteArray = GaiaUtils.convertHexaStringToByteArray(hexString);

        tSize = getTagSize(byteArray, byteArrayPosition);
        byteArrayPosition = tSize / 2;

        if ( byteArray.length == byteArrayPosition )
        {
            throw new UbiveloxException("Length Range is not exist");

        }
        else
        {
            // primitive와 constructed 구분
            if ( ((byteArray[byteArrayPosition]) & 0b0010_0000) == 0b0010_0000 )
            {
                valueType = ValueType.CONSTRUCTED;
            }
            else
            {
                valueType = ValueType.PRIMITIVE;
            }

            lSize = getLengthSize(byteArray, byteArrayPosition);
            byteArrayPosition = ((tSize + lSize) / 2) - 1;

            outPut += hexString.substring(0, tSize) + "\t" + hexString.substring(tSize, tSize + lSize);
        }
        // length가 0이면 val없이 output
        if ( byteArray[byteArrayPosition] == 0 )
        {
            return new TLVResultNBytePosition(outPut, byteArrayPosition);
        }

        if ( (tSize + lSize) == hexString.length() )
        {
            throw new UbiveloxException("Value Range is not exist");
        }

        vSize = byteArray[byteArrayPosition] * 2;
        // System.out.println("vSize : " + vSize);

        if ( (tSize + lSize + vSize) > hexString.length() )
        {
            throw new UbiveloxException("Value Range is not enough");
        }

        /*
         *
         * value 처리부분 1. return 하기 전에 다음 헥사값이 존재할 때, 현재 TLV의 다음 HexString부터 substring하여
         * 다시 parse를 돈다. 2. parse를 다시 돌릴 때, 다음의 HexString과 byteArrayPosition 값을 파라미터로
         * 넘겨줘야 한다.
         */

        outPut += "\t" + hexString.substring((tSize + lSize), (tSize + lSize + vSize));
        return new TLVResultNBytePosition(outPut, byteArrayPosition + (vSize / 2));
    }
}
