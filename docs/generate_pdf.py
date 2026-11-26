import os
import re
import xml.sax.saxutils as saxutils
from reportlab.lib.pagesizes import A4
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, Image, Preformatted, KeepTogether, HRFlowable
)
from reportlab.pdfgen import canvas

# Page dimensions for A4: 595.27 x 841.89 points
PAGE_WIDTH, PAGE_HEIGHT = A4
LEFT_MARGIN = 54
RIGHT_MARGIN = 54
TOP_MARGIN = 54
BOTTOM_MARGIN = 54
PRINTABLE_WIDTH = PAGE_WIDTH - LEFT_MARGIN - RIGHT_MARGIN

def format_inline(text):
    """Safely converts markdown inline formatting into valid ReportLab XML."""
    if not text:
        return ""
    code_blocks = []
    def save_code(m):
        code_blocks.append(m.group(1))
        return f"__CODE_PLACEHOLDER_{len(code_blocks)-1}__"
    
    # 1. Protect inline code
    t = re.sub(r'`([^`]+)`', save_code, text)
    
    # 2. XML Escape
    t = saxutils.escape(t)
    
    # 3. Bold & Italic
    t = re.sub(r'\*\*(.+?)\*\*', r'<b>\1</b>', t)
    t = re.sub(r'(?<!\*)\*([^\*]+?)\*(?!\*)', r'<i>\1</i>', t)
    
    # 4. Restore code
    for idx, c in enumerate(code_blocks):
        c_esc = saxutils.escape(c)
        t = t.replace(f"__CODE_PLACEHOLDER_{idx}__", f'<font face="Courier" color="#1E293B"><b>{c_esc}</b></font>')
        
    return t

class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_decorations(num_pages)
            super().showPage()
        super().save()

    def draw_page_decorations(self, total_pages):
        if self._pageNumber == 1:
            return  # Cover Page

        self.saveState()
        self.setFont("Helvetica", 8)
        self.setFillColor(colors.HexColor("#4A5568"))

        # Running Header
        self.drawString(LEFT_MARGIN, PAGE_HEIGHT - 36, "UCS553: Enterprise Web Application — Mini Project Report")
        self.drawRightString(PAGE_WIDTH - RIGHT_MARGIN, PAGE_HEIGHT - 36, "TIET, Patiala")
        self.setStrokeColor(colors.HexColor("#CBD5E1"))
        self.setLineWidth(0.5)
        self.line(LEFT_MARGIN, PAGE_HEIGHT - 40, PAGE_WIDTH - RIGHT_MARGIN, PAGE_HEIGHT - 40)

        # Running Footer
        self.line(LEFT_MARGIN, 44, PAGE_WIDTH - RIGHT_MARGIN, 44)
        self.drawString(LEFT_MARGIN, 32, "Doctor Smart Appointment & Clinical Workflow System")
        self.drawRightString(PAGE_WIDTH - RIGHT_MARGIN, 32, f"Page {self._pageNumber} of {total_pages}")
        self.restoreState()

def build_pdf(report_md_path, logo_path, output_pdf_path):
    doc = SimpleDocTemplate(
        output_pdf_path,
        pagesize=A4,
        leftMargin=LEFT_MARGIN,
        rightMargin=RIGHT_MARGIN,
        topMargin=TOP_MARGIN,
        bottomMargin=BOTTOM_MARGIN
    )

    NAVY = colors.HexColor("#1A2B4C")
    CRIMSON = colors.HexColor("#8B0000")
    DARK_GRAY = colors.HexColor("#2D3748")
    LIGHT_BG = colors.HexColor("#F8FAFC")
    BORDER_COLOR = colors.HexColor("#CBD5E1")

    # Cover Page Styles
    cover_top_style = ParagraphStyle(
        'CoverTop', fontName='Helvetica-Bold', fontSize=15, leading=20, alignment=1, textColor=CRIMSON, spaceAfter=4
    )
    cover_subtop_style = ParagraphStyle(
        'CoverSubTop', fontName='Helvetica-Bold', fontSize=13, leading=17, alignment=1, textColor=DARK_GRAY, spaceAfter=14
    )
    cover_title_style = ParagraphStyle(
        'CoverTitle', fontName='Helvetica-Bold', fontSize=16, leading=22, alignment=1, textColor=NAVY, spaceAfter=16
    )
    cover_guidance_header = ParagraphStyle(
        'CoverGuidanceHeader', fontName='Helvetica', fontSize=11, leading=15, alignment=1, textColor=DARK_GRAY, spaceAfter=3
    )
    cover_guidance_name = ParagraphStyle(
        'CoverGuidanceName', fontName='Helvetica-Bold', fontSize=13, leading=17, alignment=1, textColor=NAVY, spaceAfter=2
    )
    cover_guidance_detail = ParagraphStyle(
        'CoverGuidanceDetail', fontName='Helvetica', fontSize=10.5, leading=14, alignment=1, textColor=DARK_GRAY, spaceAfter=14
    )
    cover_bottom_bold = ParagraphStyle(
        'CoverBottomBold', fontName='Helvetica-Bold', fontSize=11, leading=15, alignment=1, textColor=NAVY, spaceAfter=2
    )
    cover_bottom_reg = ParagraphStyle(
        'CoverBottomReg', fontName='Helvetica', fontSize=10, leading=14, alignment=1, textColor=DARK_GRAY, spaceAfter=2
    )
    cover_bottom_italic = ParagraphStyle(
        'CoverBottomItalic', fontName='Helvetica-Oblique', fontSize=9.5, leading=13, alignment=1, textColor=DARK_GRAY, spaceAfter=2
    )

    # Document Styles
    h1_style = ParagraphStyle(
        'Heading1_Custom', fontName='Helvetica-Bold', fontSize=14.5, leading=19, textColor=NAVY, spaceBefore=14, spaceAfter=8, keepWithNext=True
    )
    h2_style = ParagraphStyle(
        'Heading2_Custom', fontName='Helvetica-Bold', fontSize=12, leading=16, textColor=CRIMSON, spaceBefore=12, spaceAfter=6, keepWithNext=True
    )
    h3_style = ParagraphStyle(
        'Heading3_Custom', fontName='Helvetica-Bold', fontSize=10.5, leading=14, textColor=NAVY, spaceBefore=9, spaceAfter=4, keepWithNext=True
    )
    body_style = ParagraphStyle(
        'Body_Custom', fontName='Helvetica', fontSize=9, leading=13.5, textColor=DARK_GRAY, spaceAfter=6
    )
    bullet_style = ParagraphStyle(
        'Bullet_Custom', fontName='Helvetica', fontSize=9, leading=13.5, textColor=DARK_GRAY, leftIndent=14, spaceAfter=3
    )
    code_text_style = ParagraphStyle(
        'CodeText', fontName='Courier', fontSize=6.8, leading=8.8, textColor=colors.HexColor("#0F172A")
    )
    table_cell_style = ParagraphStyle(
        'TableCell', fontName='Helvetica', fontSize=8, leading=11, textColor=DARK_GRAY
    )
    table_header_style = ParagraphStyle(
        'TableHeader', fontName='Helvetica-Bold', fontSize=8, leading=11, textColor=colors.white
    )

    story = []

    # =========================================================================
    # 1. COVER PAGE
    # =========================================================================
    story.append(Paragraph("UCS553: Enterprise Web Application", cover_top_style))
    story.append(Paragraph("Mini Project Report", cover_subtop_style))
    story.append(Spacer(1, 4))
    story.append(Paragraph("DOCTOR SMART APPOINTMENT &amp; CLINICAL WORKFLOW SYSTEM", cover_title_style))
    story.append(Spacer(1, 4))

    story.append(Paragraph("Under the Guidance of", cover_guidance_header))
    story.append(Paragraph("Dr. Jayendra Barua", cover_guidance_name))
    story.append(Paragraph("Assistant Professor<br/>Computer Science &amp; Engineering Department<br/>Thapar Institute of Engineering &amp; Technology, Patiala", cover_guidance_detail))
    story.append(Spacer(1, 4))

    if os.path.exists(logo_path):
        logo_img = Image(logo_path, width=125, height=135)
        logo_img.hAlign = 'CENTER'
        story.append(logo_img)
        story.append(Spacer(1, 8))
    else:
        story.append(Spacer(1, 20))

    story.append(Paragraph("<b>Submitted By:</b>", cover_guidance_header))
    story.append(Spacer(1, 3))

    student_data = [
        [Paragraph("<b>Roll Number</b>", table_header_style), Paragraph("<b>Student Name</b>", table_header_style), Paragraph("<b>Batch</b>", table_header_style)],
        [Paragraph("1024030368", table_cell_style), Paragraph("Mohit Srivastav", table_cell_style), Paragraph("3C41", table_cell_style)],
        [Paragraph("1024030384", table_cell_style), Paragraph("Shreyas Tiwari", table_cell_style), Paragraph("3C41", table_cell_style)]
    ]
    student_table = Table(student_data, colWidths=[110, 160, 80], hAlign='CENTER')
    student_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), NAVY),
        ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
        ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
        ('GRID', (0, 0), (-1, -1), 0.5, BORDER_COLOR),
        ('TOPPADDING', (0, 0), (-1, -1), 4),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 4),
    ]))
    story.append(student_table)
    story.append(Spacer(1, 14))

    story.append(Paragraph("Computer Science and Engineering Department", cover_bottom_bold))
    story.append(Paragraph("Thapar Institute of Engineering &amp; Technology, Patiala", cover_bottom_reg))
    story.append(Paragraph("(Deemed to be University)", cover_bottom_italic))
    story.append(Spacer(1, 4))
    story.append(Paragraph("In Partial Fulfilment of the Requirements for the Degree of", cover_bottom_italic))
    story.append(Paragraph("Bachelor of Engineering in Computer Engineering", cover_bottom_bold))
    story.append(Paragraph("at", cover_bottom_italic))
    story.append(Paragraph("Thapar Institute of Engineering &amp; Technology, Patiala", cover_bottom_reg))
    story.append(Spacer(1, 4))
    story.append(Paragraph("November 2026", cover_bottom_bold))

    story.append(PageBreak())

    # =========================================================================
    # 2. PARSE MARKDOWN CONTENT
    # =========================================================================
    with open(report_md_path, 'r', encoding='utf-8') as f:
        md_text = f.read()

    # Skip markdown header block
    first_break = md_text.find('<div style="page-break-after: always;"></div>')
    if first_break != -1:
        content_md = md_text[first_break + len('<div style="page-break-after: always;"></div>'):]
    else:
        content_md = md_text

    lines = content_md.split('\n')
    i = 0
    in_code_block = False
    code_lines = []
    table_lines = []

    while i < len(lines):
        line = lines[i]
        stripped = line.strip()

        if '<div style="page-break-after: always;"></div>' in line:
            story.append(PageBreak())
            i += 1
            continue

        # Code block delimiter
        if stripped.startswith('```'):
            if in_code_block:
                in_code_block = False
                code_text = '\n'.join(code_lines)
                code_lines = []
                p_code = Preformatted(code_text, code_text_style)
                block_table = Table([[p_code]], colWidths=[PRINTABLE_WIDTH])
                block_table.setStyle(TableStyle([
                    ('BACKGROUND', (0, 0), (-1, -1), LIGHT_BG),
                    ('BOX', (0, 0), (-1, -1), 0.5, BORDER_COLOR),
                    ('LEFTPADDING', (0, 0), (-1, -1), 7),
                    ('RIGHTPADDING', (0, 0), (-1, -1), 7),
                    ('TOPPADDING', (0, 0), (-1, -1), 5),
                    ('BOTTOMPADDING', (0, 0), (-1, -1), 5),
                ]))
                story.append(Spacer(1, 4))
                story.append(block_table)
                story.append(Spacer(1, 6))
            else:
                in_code_block = True
                code_lines = []
            i += 1
            continue

        if in_code_block:
            code_lines.append(line)
            i += 1
            continue

        # Table rows
        if stripped.startswith('|') and stripped.endswith('|'):
            table_lines.append(stripped)
            i += 1
            continue
        elif table_lines:
            rows_data = []
            is_header = True
            for t_line in table_lines:
                cells = [c.strip() for c in t_line.split('|')[1:-1]]
                if all(re.match(r'^\:?-+\:?$', c) for c in cells if c):
                    is_header = False
                    continue
                row_cells = []
                for cell in cells:
                    fmt_cell = format_inline(cell)
                    if is_header:
                        row_cells.append(Paragraph(fmt_cell, table_header_style))
                    else:
                        row_cells.append(Paragraph(fmt_cell, table_cell_style))
                rows_data.append(row_cells)

            if rows_data:
                num_cols = len(rows_data[0])
                col_w = PRINTABLE_WIDTH / max(num_cols, 1)
                tbl = Table(rows_data, colWidths=[col_w] * num_cols)
                t_style = [
                    ('BACKGROUND', (0, 0), (-1, 0), NAVY),
                    ('TEXTCOLOR', (0, 0), (-1, 0), colors.white),
                    ('GRID', (0, 0), (-1, -1), 0.5, BORDER_COLOR),
                    ('VALIGN', (0, 0), (-1, -1), 'TOP'),
                    ('TOPPADDING', (0, 0), (-1, -1), 3),
                    ('BOTTOMPADDING', (0, 0), (-1, -1), 3),
                    ('LEFTPADDING', (0, 0), (-1, -1), 4),
                    ('RIGHTPADDING', (0, 0), (-1, -1), 4),
                ]
                for r in range(1, len(rows_data)):
                    if r % 2 == 0:
                        t_style.append(('BACKGROUND', (0, r), (-1, r), colors.HexColor("#F1F5F9")))
                tbl.setStyle(TableStyle(t_style))
                story.append(Spacer(1, 4))
                story.append(tbl)
                story.append(Spacer(1, 6))
            table_lines = []

        if not stripped:
            i += 1
            continue

        if stripped in ('---', '***', '___'):
            story.append(HRFlowable(width="100%", thickness=0.5, color=BORDER_COLOR, spaceBefore=5, spaceAfter=7))
            i += 1
            continue

        # Headings
        if stripped.startswith('# '):
            text = format_inline(stripped[2:].strip())
            story.append(Spacer(1, 7))
            story.append(Paragraph(text, h1_style))
            i += 1
            continue
        elif stripped.startswith('## '):
            text = format_inline(stripped[3:].strip())
            story.append(Spacer(1, 5))
            story.append(Paragraph(text, h2_style))
            i += 1
            continue
        elif stripped.startswith('### '):
            text = format_inline(stripped[4:].strip())
            story.append(Spacer(1, 4))
            story.append(Paragraph(text, h3_style))
            i += 1
            continue
        elif stripped.startswith('#### '):
            text = format_inline(stripped[5:].strip())
            story.append(Spacer(1, 3))
            story.append(Paragraph(f"<b>{text}</b>", h3_style))
            i += 1
            continue

        # Lists
        if stripped.startswith(('- ', '* ')):
            bullet_text = format_inline(stripped[2:].strip())
            story.append(Paragraph(f"&bull;&nbsp;&nbsp;{bullet_text}", bullet_style))
            i += 1
            continue
        elif re.match(r'^\d+\.\s', stripped):
            match = re.match(r'^(\d+\.)\s*(.*)', stripped)
            prefix = match.group(1)
            num_text = format_inline(match.group(2))
            story.append(Paragraph(f"<b>{prefix}</b>&nbsp;&nbsp;{num_text}", bullet_style))
            i += 1
            continue

        # Standard Paragraph
        p_text = format_inline(stripped)
        story.append(Paragraph(p_text, body_style))
        i += 1

    # End table flush if any
    if table_lines:
        rows_data = []
        is_header = True
        for t_line in table_lines:
            cells = [c.strip() for c in t_line.split('|')[1:-1]]
            if all(re.match(r'^\:?-+\:?$', c) for c in cells if c):
                is_header = False
                continue
            row_cells = []
            for cell in cells:
                fmt_cell = format_inline(cell)
                if is_header:
                    row_cells.append(Paragraph(fmt_cell, table_header_style))
                else:
                    row_cells.append(Paragraph(fmt_cell, table_cell_style))
            rows_data.append(row_cells)
        if rows_data:
            num_cols = len(rows_data[0])
            col_w = PRINTABLE_WIDTH / max(num_cols, 1)
            tbl = Table(rows_data, colWidths=[col_w] * num_cols)
            tbl.setStyle(TableStyle([
                ('BACKGROUND', (0, 0), (-1, 0), NAVY),
                ('GRID', (0, 0), (-1, -1), 0.5, BORDER_COLOR),
                ('TOPPADDING', (0, 0), (-1, -1), 3),
                ('BOTTOMPADDING', (0, 0), (-1, -1), 3),
            ]))
            story.append(tbl)

    # Build PDF
    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"Successfully generated PDF: {output_pdf_path}")

if __name__ == '__main__':
    md_file = os.path.abspath('docs/MINI_PROJECT_REPORT.md')
    logo_file = os.path.abspath('docs/thapar_logo.jpeg')
    pdf_out = os.path.abspath('docs/MINI_PROJECT_REPORT.pdf')
    build_pdf(md_file, logo_file, pdf_out)
