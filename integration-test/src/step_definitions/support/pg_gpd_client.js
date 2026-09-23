import {Pool} from "pg";

const username = process.env.PG_GPD_USERNAME;
const password = process.env.PG_GPD_PASSWORD;
const serverName = process.env.PG_GPD_SERVER_NAME;
const databaseName = process.env.PG_GPD_DATABASE_NAME;

const pool = new Pool({
    user: username,
    database: databaseName,
    password: password,
    host: serverName,
    port: 5432,
    ssl: true
});

export async function shutDownPool() {
    await pool.end();
}

const IUPD = "IUPD_INTEGRATION_TEST_EBOLLO_SERVICE";

export async function insertDebtPosition({iuv, fiscalCode}) {
    const client = await pool.connect();
    try {
        await client.query("BEGIN");

        const positionRes = await client.query(
            `INSERT INTO apd.apd.payment_position (id, city, civic_number, company_name, country, email,
                                                   fiscal_code, full_name, inserted_date, iupd,
                                                   last_updated_date, max_due_date, min_due_date,
                                                   office_name, organization_fiscal_code, phone,
                                                   postal_code, province, publish_date, region, status,
                                                   street_name, "type", validity_date, "version",
                                                   switch_to_expired, payment_date, pull, pay_stand_in,
                                                   service_type)
             VALUES (nextval('apd.payment_pos_seq'), 'Pizzo Calabro', '11', 'PagoPA S.p.A.', 'IT',
                     'john.doe@test.test', 'JHNDOE00A01B157N', 'John Doe',
                     '2024-11-12 16:09:43.477', $1,
                     '2024-11-12 16:09:43.479', '2024-12-12 16:09:43.323', '2024-12-12 16:09:43.323',
                     'SkyLab - Sede via Washington', $2, '333-123456789', '89812',
                     'VV', '2024-11-12 16:09:43.479', 'CA', 'PAID', 'via Washington', 'F',
                     '2024-11-12 16:09:43.479', 0, false, '2024-11-12 17:09:43.477', true, false,
                     'EBOLLO') RETURNING id`,
            [IUPD, fiscalCode]
        );
        const paymentPositionId = positionRes.rows[0].id;

        const optionRes = await client.query(
            `INSERT INTO apd.apd.payment_option (id, amount, description, due_date, fee,
                                                 flow_reporting_id, receipt_id, inserted_date,
                                                 is_partial_payment, iuv, last_updated_date,
                                                 organization_fiscal_code, payment_date, payment_method,
                                                 psp_company, reporting_date, retention_date, status,
                                                 payment_position_id, notification_fee,
                                                 last_updated_date_notification_fee, nav, fiscal_code,
                                                 full_name, "type", street_name, civic_number,
                                                 postal_code, city, province, region, country, email,
                                                 phone, send_sync,
                                                 psp_code, psp_tax_code,
                                                 payment_plan_id,
                                                 switch_to_expired, validity_date,
                                                 payment_option_description)
             VALUES (nextval('apd.payment_opt_seq'), 1600, 'Pagamento marca da bollo digitale',
                     '2024-12-12 16:09:43.323', 50,
                     NULL, '380842b0b83f4d0fbfc1062378f20a28',
                     '2024-11-12 16:09:43.477', false, $1, '2024-11-12 16:09:43.477',
                     $2, '2024-11-12 17:09:43.477', 'creditCard', 'Intesa Sanpaolo S.p.A',
                     NULL, NULL, 'PO_PAID',
                     $3, 0, NULL, $4, 'JHNDOE00A01B157N', 'John Doe', 'F', NULL,
                     NULL, '89812', NULL, 'RM', 'CA', NULL,
                     'john.doe@test.test', NULL, false, 'BCITITMM', '00799960158',
                     'SINGLE_OPTION', true, '2026-09-16 14:31:12.885',
                     'Pagamento marca da bollo digitale') RETURNING id`,
            [iuv, fiscalCode, paymentPositionId, `3${iuv}`]
        );
        const paymentOptionId = optionRes.rows[0].id;

        const transferRes = await client.query(
            `INSERT INTO apd.apd.transfer (id, amount, category, iban, transfer_id, inserted_date, iuv,
                                           last_updated_date, organization_fiscal_code, postal_iban,
                                           remittance_information, status, payment_option_id,
                                           hash_document, stamp_type, provincial_residence)
             VALUES (nextval('apd.transfer_seq'), 1600, '6/0811100IM/', NULL, '1', '2024-11-12 16:09:43.477',
                     $1, '2024-11-12 16:09:43.477', $2, NULL,
                     '/RFB/352178956907278839/CNR/MRRNSR75R05H501I/TXT/Pagamento marca da bollo digitale',
                     'T_UNREPORTED', $3, '47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=',
                     '01', 'RM') RETURNING id`,
            [iuv, fiscalCode, paymentOptionId]
        );
        const transferId = transferRes.rows[0].id;

        await client.query("COMMIT");

        return {paymentPositionId, paymentOptionId, transferId};
    } catch (err) {
        await client.query("ROLLBACK");
        console.error(`[pg-gpd] Error inserting debt position (iuv=${iuv}, fiscalCode=${fiscalCode}): ${err.message}`, err);
        throw err;
    } finally {
        client.release();
    }
}

export async function deleteDebtPosition(paymentPositionId) {
    const client = await pool.connect();
    try {
        await client.query("BEGIN");

        await client.query(
            `DELETE
             FROM apd.apd.transfer
             WHERE payment_option_id IN (SELECT id
                                         FROM apd.apd.payment_option
                                         WHERE payment_position_id = $1)`,
            [paymentPositionId]
        );

        await client.query(
            `DELETE
             FROM apd.apd.payment_option
             WHERE payment_position_id = $1`,
            [paymentPositionId]
        );

        await client.query(
            `DELETE
             FROM apd.apd.payment_position
             WHERE id = $1`,
            [paymentPositionId]
        );

        await client.query("COMMIT");
    } catch (err) {
        await client.query("ROLLBACK");
        console.error(`[pg-gpd] Error deleting debt position (paymentPositionId=${paymentPositionId}): ${err.message}`, err);
        throw err;
    } finally {
        client.release();
    }
}
