-- --------------------------------------------------------------------------------
-- Routine DDL
-- Note: comments before and after the routine body will not be stored by the server
-- --------------------------------------------------------------------------------
DELIMITER $$

CREATE DEFINER=`vasja`@`%` PROCEDURE `cuentas_mensual`(IN p_fechaMin datetime, IN p_fechaMax datetime, IN p_cat_id BIGINT)
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE v_numero bigint;
    DECLARE v_cuenta_id bigint;
    DECLARE v_nombre VARCHAR(255);
	DECLARE v_pen_cargo DECIMAL(20,2);
    DECLARE v_pen_abono DECIMAL(20,2);
    DECLARE v_usd_cargo DECIMAL(20,2);
    DECLARE v_usd_abono DECIMAL(20,2);
    DECLARE v_pen_ant DECIMAL(20,2);
    DECLARE v_usd_ant DECIMAL(20,2);
    DECLARE v_pen_now DECIMAL(20,2);
    DECLARE v_usd_now DECIMAL(20,2);
    DECLARE cursorCtas CURSOR FOR SELECT `id`,`numero`,`nombre` FROM `CUENTA` WHERE `CATEGORIACUENTA_ID` = p_cat_id ORDER BY ID ASC;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

CREATE TABLE IF NOT EXISTS `CUENTA_MENSUAL`  (
	  `CUENTA_NUMERO` BIGINT DEFAULT NULL,
	  `CUENTA_NOMBRE` VARCHAR(255) DEFAULT NULL,
	  `PEN_ANTERIOR` decimal(38,2) DEFAULT NULL,
	  `PEN_INGRESOS` decimal(38,2) DEFAULT NULL,
      `PEN_EGRESOS` decimal(38,2) DEFAULT NULL,
      `PEN_FINAL` decimal(38,2) DEFAULT NULL,
      `USD_ANTERIOR` decimal(38,2) DEFAULT NULL,
	  `USD_INGRESOS` decimal(38,2) DEFAULT NULL,
      `USD_EGRESOS` decimal(38,2) DEFAULT NULL,
      `USD_FINAL` decimal(38,2) DEFAULT NULL
);
DELETE FROM `CUENTA_MENSUAL`;

OPEN cursorCtas;
   read_loop: LOOP
     SET done = FALSE;
     FETCH cursorCtas INTO v_cuenta_id, v_numero, v_nombre;
     IF done THEN
        LEAVE read_loop;
     END IF;
     IF NOT done THEN
       SET v_pen_cargo = 0;
       SET v_pen_abono = 0;
       SET v_usd_cargo = 0;
       SET v_usd_abono = 0;
       SET v_pen_ant = 0;
       SET v_pen_ant = 0;
       SET v_usd_now = 0;
       SET v_usd_now = 0;
       SELECT
             COALESCE(SUM(operacion.`PEN`),0.00) AS operacion_PEN     
        FROM
             `operacion` operacion INNER JOIN `CUENTA` CUENTA ON operacion.`CUENTA_ID` = CUENTA.`ID`     
        WHERE
             operacion.FECHA >= p_fechaMin 
         AND operacion.FECHA < p_fechaMax
         AND operacion.CUENTA_ID = v_cuenta_id
         AND operacion.`ISCARGO`=1 INTO v_pen_cargo;
        -- IF (v_pen_cargo IS NULL) THEN v_pen_cargo=0.00;
        SELECT
             COALESCE(SUM(operacion.`PEN`),0.00) AS operacion_PEN     
        FROM
             `operacion` operacion INNER JOIN `CUENTA` CUENTA ON operacion.`CUENTA_ID` = CUENTA.`ID`     
        WHERE
             operacion.FECHA >= p_fechaMin 
         AND operacion.FECHA < p_fechaMax
         AND operacion.CUENTA_ID = v_cuenta_id
         AND operacion.`ISCARGO`=0 INTO v_pen_abono;
        SELECT
             COALESCE(SUM(operacion.`USD`),0.00)     
        FROM
             `operacion` operacion INNER JOIN `CUENTA` CUENTA ON operacion.`CUENTA_ID` = CUENTA.`ID`     
        WHERE
             operacion.FECHA >= p_fechaMin 
         AND operacion.FECHA < p_fechaMax
         AND operacion.CUENTA_ID = v_cuenta_id
         AND operacion.`ISCARGO`=1 INTO v_usd_cargo;
        SELECT
             COALESCE(SUM(operacion.`USD`),0.00)
        FROM
             `operacion` operacion INNER JOIN `CUENTA` CUENTA ON operacion.`CUENTA_ID` = CUENTA.`ID`     
        WHERE
             operacion.FECHA >= p_fechaMin 
         AND operacion.FECHA < p_fechaMax
         AND operacion.CUENTA_ID = v_cuenta_id
         AND operacion.`ISCARGO`=0 INTO v_usd_abono;
        
        -- ANTERIOR
        SELECT
             COALESCE(operacion.`SALDOPEN`,0.00)
        FROM
             `operacion` operacion INNER JOIN `CUENTA` CUENTA ON operacion.`CUENTA_ID` = CUENTA.`ID`
        WHERE
             operacion.FECHA < p_fechaMin
         AND operacion.CUENTA_ID = v_cuenta_id
        ORDER BY operacion.FECHA DESC, operacion.ID DESC LIMIT 1 INTO v_pen_ant;

        SELECT
             COALESCE(operacion.`SALDOUSD`,0.00)
        FROM
             `operacion` operacion INNER JOIN `CUENTA` CUENTA ON operacion.`CUENTA_ID` = CUENTA.`ID`
        WHERE
             operacion.FECHA < p_fechaMin
         AND operacion.CUENTA_ID = v_cuenta_id
        ORDER BY operacion.FECHA DESC, operacion.ID DESC LIMIT 1 INTO v_usd_ant;

        -- ACTUAL
        SELECT
             COALESCE(operacion.`SALDOPEN`,0.00)
        FROM
             `operacion` operacion INNER JOIN `CUENTA` CUENTA ON operacion.`CUENTA_ID` = CUENTA.`ID`
        WHERE
             operacion.FECHA < p_fechaMax
         AND operacion.CUENTA_ID = v_cuenta_id
        ORDER BY operacion.FECHA DESC, operacion.ID DESC LIMIT 1 INTO v_pen_now;

        SELECT
             COALESCE(operacion.`SALDOUSD`,0.00)
        FROM
             `operacion` operacion INNER JOIN `CUENTA` CUENTA ON operacion.`CUENTA_ID` = CUENTA.`ID`
        WHERE
             operacion.FECHA < p_fechaMax
         AND operacion.CUENTA_ID = v_cuenta_id
        ORDER BY operacion.FECHA DESC, operacion.ID DESC LIMIT 1 INTO v_usd_now;

        INSERT INTO `CUENTA_MENSUAL` VALUES(v_numero, v_nombre, v_pen_ant, v_pen_abono, v_pen_cargo, v_pen_now, v_usd_ant, v_usd_abono, v_usd_cargo, v_usd_now);
      END IF;
    END LOOP;
CLOSE cursorCtas;
END