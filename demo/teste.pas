Program CalcularSalario
Var TempoEmAnos, ValorSalario, testegeracaoCodigo, teste2, testeFor, testeWhile, testeRepeat : Integer; 
Begin 
    read(testegeracaoCodigo, teste2);

    For testeFor := 0 to 20 do
    Begin
        Write(ValorSalario);
    End;

    While (testeWhile < 10) do
        Begin
            ValorSalario := ValorSalario + 5;
        End;

    Repeat
        Write(ValorSalario);
        ValorSalario := ValorSalario + 5;
    until (testeRepeat >= 10);


    If(testegeracaoCodigo<3) then
        Begin
            TempoEmAnos := TempoEmAnos-3;
        End
    Else 
        Begin
            ValorSalario := ValorSalario * 2;
        End;
    
End.