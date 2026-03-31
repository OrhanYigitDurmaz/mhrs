"""separate_user_and_profile_models

Revision ID: c93172e1c921
Revises:
Create Date: 2026-03-27 17:41:25.787690

"""
from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects import postgresql

# revision identifiers, used by Alembic.
revision = 'c93172e1c921'
down_revision = None
branch_labels = None
depends_on = None


def upgrade() -> None:
    # Create the new kullanicilar table
    op.create_table(
        'kullanicilar',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('tc_no', sa.String(length=11), nullable=False),
        sa.Column('sifre_hash', sa.String(length=255), nullable=False),
        sa.Column('rol', sa.Enum('ADMIN', 'DOKTOR', 'HASTA', name='rol'), nullable=False),
        sa.Column('adi_soyadi', sa.String(length=100), nullable=False),
        sa.Column('telefon', sa.String(length=15), nullable=True),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index('ix_kullanicilar_id', 'kullanicilar', ['id'])
    op.create_index('ix_kullanicilar_tc_no', 'kullanicilar', ['tc_no'], unique=True)

    # Add kullanici_id column to doktorlar table (nullable initially)
    op.add_column('doktorlar', sa.Column('kullanici_id', sa.Integer(), nullable=True))

    # Add unique constraint to kullanici_id in doktorlar
    op.create_unique_constraint('uq_doktorlar_kullanici_id', 'doktorlar', ['kullanici_id'])

    # Migrate existing doctor data from hastalar to kullanicilar
    # First, insert all existing hasta records into kullanicilar
    op.execute("""
        INSERT INTO kullanicilar (tc_no, sifre_hash, rol, adi_soyadi, telefon)
        SELECT tc_no, sifre_hash,
            CASE
                WHEN rol = 'user' THEN 'HASTA'
                WHEN rol = 'admin' THEN 'ADMIN'
                ELSE UPPER(rol)
            END,
            adi_soyadi, telefon
        FROM hastalar
    """)

    # For doctors, we need to link them to their kullanici records
    # First, add a temporary column to store the old hasta_id for doctors
    op.add_column('doktorlar', sa.Column('temp_hasta_id', sa.Integer(), nullable=True))

    # Create a mapping: for each doktor, find their corresponding kullanici by name
    op.execute("""
        UPDATE doktorlar d
        SET temp_hasta_id = (
            SELECT h.id FROM hastalar h
            WHERE h.adi_soyadi = d.adi_soyadi
            AND h.rol = 'doktor'
            LIMIT 1
        )
    """)

    # Update doktorlar.kullanici_id based on the temp_hasta_id
    op.execute("""
        UPDATE doktorlar d
        SET kullanici_id = (
            SELECT k.id FROM kullanicilar k
            WHERE k.tc_no = (SELECT h.tc_no FROM hastalar h WHERE h.id = d.temp_hasta_id)
        )
        WHERE temp_hasta_id IS NOT NULL
    """)

    # Drop the temporary column
    op.drop_column('doktorlar', 'temp_hasta_id')

    # Make kullanici_id not nullable after data migration
    op.alter_column('doktorlar', 'kullanici_id', nullable=False)

    # Create foreign key constraint for doktorlar.kullanici_id
    op.create_foreign_key(
        'fk_doktorlar_kullanici_id', 'doktorlar', 'kullanicilar',
        ['kullanici_id'], ['id']
    )

    # Drop adi_soyadi from doktorlar
    op.drop_column('doktorlar', 'adi_soyadi')

    # Add kullanici_id column to hastalar table (nullable initially)
    op.add_column('hastalar', sa.Column('kullanici_id', sa.Integer(), nullable=True))

    # Create unique constraint to kullanici_id in hastalar
    op.create_unique_constraint('uq_hastalar_kullanici_id', 'hastalar', ['kullanici_id'])

    # Update hastalar.kullanici_id to link to the new kullanicilar records
    op.execute("""
        UPDATE hastalar h
        SET kullanici_id = (
            SELECT k.id FROM kullanicilar k
            WHERE k.tc_no = h.tc_no
        )
    """)

    # Make kullanici_id not nullable after data migration
    op.alter_column('hastalar', 'kullanici_id', nullable=False)

    # Create foreign key constraint for hastalar.kullanici_id
    op.create_foreign_key(
        'fk_hastalar_kullanici_id', 'hastalar', 'kullanicilar',
        ['kullanici_id'], ['id']
    )

    # Now drop old columns from hastalar (keep id for FK relationships)
    # We need to keep the id column, but drop auth-related fields
    # First drop the enum column
    op.execute('ALTER TABLE hastalar ALTER COLUMN rol DROP DEFAULT')

    # Drop the old columns
    op.drop_column('hastalar', 'tc_no')
    op.drop_column('hastalar', 'sifre_hash')
    op.drop_column('hastalar', 'rol')
    op.drop_column('hastalar', 'adi_soyadi')
    op.drop_column('hastalar', 'telefon')

    # Update the randevular table to use the new relationship
    # No changes needed here as it references hastalar.id which we kept


def downgrade() -> None:
    # Reverse the migration - add back columns to hastalar
    op.add_column('hastalar', sa.Column('telefon', sa.String(length=15), nullable=True))
    op.add_column('hastalar', sa.Column('adi_soyadi', sa.String(length=100), nullable=False))
    op.add_column('hastalar', sa.Column('rol', sa.Enum('USER', 'DOKTOR', 'ADMIN', name='rol'), nullable=False, server_default='USER'))
    op.add_column('hastalar', sa.Column('sifre_hash', sa.String(length=255), nullable=False))
    op.add_column('hastalar', sa.Column('tc_no', sa.String(length=11), nullable=False))

    # Migrate data back from kullanicilar to hastalar
    op.execute("""
        UPDATE hastalar h
        SET tc_no = (SELECT k.tc_no FROM kullanicilar k WHERE k.id = h.kullanici_id),
            sifre_hash = (SELECT k.sifre_hash FROM kullanicilar k WHERE k.id = h.kullanici_id),
            adi_soyadi = (SELECT k.adi_soyadi FROM kullanicilar k WHERE k.id = h.kullanici_id),
            telefon = (SELECT k.telefon FROM kullanicilar k WHERE k.id = h.kullanici_id),
            rol = CASE
                WHEN (SELECT k.rol FROM kullanicilar k WHERE k.id = h.kullanici_id) = 'HASTA' THEN 'user'
                WHEN (SELECT k.rol FROM kullanicilar k WHERE k.id = h.kullanici_id) = 'ADMIN' THEN 'admin'
                ELSE 'doktor'
            END
    """)

    # Drop foreign key from hastalar
    op.drop_constraint('fk_hastalar_kullanici_id', 'hastalar', type_='foreignkey')
    op.drop_constraint('uq_hastalar_kullanici_id', 'hastalar', type_='unique')
    op.drop_column('hastalar', 'kullanici_id')

    # Add back adi_soyadi to doktorlar
    op.add_column('doktorlar', sa.Column('adi_soyadi', sa.String(length=100), nullable=False))

    # Migrate data back from kullanicilar to doktorlar
    op.execute("""
        UPDATE doktorlar d
        SET adi_soyadi = (SELECT k.adi_soyadi FROM kullanicilar k WHERE k.id = d.kullanici_id)
    """)

    # Drop foreign key from doktorlar
    op.drop_constraint('fk_doktorlar_kullanici_id', 'doktorlar', type_='foreignkey')
    op.drop_constraint('uq_doktorlar_kullanici_id', 'doktorlar', type_='unique')
    op.drop_column('doktorlar', 'kullanici_id')

    # Drop the kullanicilar table
    op.drop_index('ix_kullanicilar_tc_no', table_name='kullanicilar')
    op.drop_index('ix_kullanicilar_id', table_name='kullanicilar')
    op.drop_table('kullanicilar')
