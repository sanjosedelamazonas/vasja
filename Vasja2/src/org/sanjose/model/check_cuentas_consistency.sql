-- --------------------------------------------------------------------------------
-- Routine DDL
-- Note: comments before and after the routine body will not be stored by the server
-- --------------------------------------------------------------------------------
DELIMITER $$

CREATE DEFINER=`vasja`@`%` PROCEDURE `check_cuentas_consistency`()
BEGIN
   DECLARE done INT DEFAULT 0;
   DECLARE consist INT DEFAULT 0;
	DECLARE v_saldo_pen,v_saldo_usd,
		    v_cuenta_pen,v_cuenta_usd,
		    v_oper_pen,v_oper_usd	DECIMAL(20,2);
   DECLARE v_numero bigint;
   DECLARE v_cuenta_id bigint;
   DECLARE v_nombre VARCHAR(255);
   DECLARE cursorCtas CURSOR FOR SELECT `id`,`numero`,`nombre` FROM `cuenta` ORDER BY NUMERO ASC;
   DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

   CREATE TABLE IF NOT EXISTS `CONSISTENCY_cuentaS`  (
	  `FECHA` datetime DEFAULT NULL,
     `ID` bigint,
     `NUMERO` bigint,
     `NOMBRE` varchar(255),
	  `cuenta_PEN` decimal(38,2) DEFAULT NULL,
	  `cuenta_USD` decimal(38,2) DEFAULT NULL,
     `SUM_OPER_PEN` decimal(38,2) DEFAULT NULL,
	  `SUM_OPER_USD` decimal(38,2) DEFAULT NULL,
     `IS_CONSISTENT` tinyint
	);
	DELETE FROM `CONSISTENCY_cuentaS`;

   OPEN cursorCtas;
    read_loop: LOOP
     SET done = FALSE;
     FETCH cursorCtas INTO v_cuenta_id, v_numero, v_nombre;
     IF done THEN
        LEAVE read_loop;
     END IF;
     IF NOT done THEN
        SET consist = 0;
        SELECT PEN FROM `cuenta` WHERE `cuenta`.`ID`=v_cuenta_id INTO v_cuenta_pen;
        SELECT USD FROM `cuenta` WHERE `cuenta`.`ID`=v_cuenta_id INTO v_cuenta_usd;
        SELECT COALESCE(SUM(operacion.PEN),0,00) FROM `operacion`, `cuenta` WHERE `operacion`.`cuenta_ID`= `cuenta`.`ID` AND `cuenta`.`ID`=v_cuenta_id INTO v_oper_pen;
        SELECT COALESCE(SUM(operacion.USD),0.00) FROM `operacion`, `cuenta` WHERE `operacion`.`cuenta_ID`= `cuenta`.`ID` AND `cuenta`.`ID`=v_cuenta_id INTO v_oper_usd;
        
        IF v_cuenta_pen=v_oper_pen AND v_cuenta_usd=v_oper_usd THEN
            SET consist=1;
        END IF;                        

        INSERT INTO `CONSISTENCY_CUENTAS` VALUES (NOW(),v_cuenta_id, v_numero, v_nombre, v_cuenta_pen, v_cuenta_usd, v_oper_pen, v_oper_usd, consist);
     END IF;
    END LOOP;
   CLOSE cursorCtas;
END