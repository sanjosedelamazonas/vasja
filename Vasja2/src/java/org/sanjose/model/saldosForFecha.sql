-- --------------------------------------------------------------------------------
-- Routine DDL
-- Note: comments before and after the routine body will not be stored by the server
-- --------------------------------------------------------------------------------
DELIMITER $$

CREATE DEFINER=`vasja`@`%` PROCEDURE `saldosForFecha`(IN p_fecha_min VARCHAR(255), IN p_fecha_max VARCHAR(255))
BEGIN
   DECLARE done INT DEFAULT 0;
	DECLARE v_catId bigint;
   DECLARE v_nombre VARCHAR(255);
   DECLARE v_pen DECIMAL(20,2);
   DECLARE v_usd DECIMAL(20,2);
   DECLARE v_fecha DATETIME;
   DECLARE cursorCats CURSOR FOR    
    SELECT ID, NOMBRE FROM categoriacuenta WHERE NOMBRE NOT LIKE 'Caja y Bancos' AND NOMBRE NOT LIKE 'Caja y BANCOs';
	DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

   CREATE TABLE IF NOT EXISTS `saldosDiario` (
        `fecha` DATETIME,
        `PEN` DECIMAL(20,2),
        `USD` DECIMAL(20,2),
        `NOMBRE` VARCHAR(255),
        `IS_INICIAL` TINYINT
    );
    DELETE FROM `saldosDiario`;
    OPEN cursorCats;                   
	    read_loop: LOOP
	      SET done = FALSE;	
	      FETCH cursorCats INTO v_catId, v_nombre;                           
	      IF done THEN
            LEAVE read_loop;
	      END IF;
           SET v_pen = 0.00;
           SET v_usd = 0.00;
           SELECT operacion.FECHA,COALESCE(operacion.SALDOPEN,0.00), COALESCE(operacion.SALDOUSD,0.00) FROM operacion, cuenta, categoriacuenta 
			 WHERE operacion.CUENTA_ID = CUENTA.ID AND categoriacuenta.CAJACUENTA_ID = CUENTA.ID AND categoriacuenta.ID = v_catId 
			 AND `operacion`.`FECHA` < p_fecha_min ORDER BY FECHA DESC, operacion.ID DESC LIMIT 1 INTO v_fecha, v_pen, v_usd; 
           -- SELECT SALDO.FECHA,COALESCE(SALDO.EFPEN,0.00), COALESCE(SALDO.EFUSD,0.00) FROM SALDO WHERE SALDO.categoriacuenta_ID=v_catId AND `SALDO`.`FECHA` < p_fecha_min 
           -- ORDER BY fecha DESC LIMIT 1 INTO v_fecha, v_pen, v_usd;
           INSERT INTO `saldosDiario`(`fecha`,`PEN`,`USD`,`NOMBRE`, `IS_INICIAL`) VALUES (v_fecha, v_pen, v_usd, v_nombre,1);		  
           SET v_pen = 0.00;
           SET v_usd = 0.00;           
           SELECT operacion.FECHA,COALESCE(operacion.SALDOPEN,0.00), COALESCE(operacion.SALDOUSD,0.00) FROM operacion, CUENTA, categoriacuenta 
			 WHERE operacion.CUENTA_ID = CUENTA.ID AND categoriacuenta.CAJACUENTA_ID = CUENTA.ID AND categoriacuenta.ID = v_catId 
			 AND `operacion`.`FECHA` < p_fecha_max ORDER BY FECHA DESC, operacion.ID DESC LIMIT 1 INTO v_fecha, v_pen, v_usd; 
--           SELECT SALDO.FECHA,COALESCE(SALDO.EFPEN,0.00), COALESCE(SALDO.EFUSD,0.00) FROM SALDO WHERE SALDO.categoriacuenta_ID=v_catId AND `SALDO`.`FECHA` < p_fecha_max 
 --           ORDER BY fecha DESC LIMIT 1 INTO v_fecha, v_pen, v_usd;
           INSERT INTO `saldosDiario`(`fecha`,`PEN`,`USD`,`NOMBRE`, `IS_INICIAL`) VALUES (v_fecha, v_pen, v_usd, v_nombre,0);		  
	    END LOOP;
     CLOSE cursorCats;
END