package com.motorworkshop.util;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * UI styling and component factory to provide a cohesive, professional, human-crafted
 * desktop look and feel for the motorcycle workshop management application.
 */
public class UIHelper {
    // Primary palette: Industrial Automotive & Workshop theme
    public static final Color PRIMARY_COLOR = new Color(29, 78, 216);       // Rich automotive cobalt blue (#1D4ED8)
    public static final Color PRIMARY_HOVER = new Color(30, 64, 175);       // Deep royal navy hover (#1E40AF)
    public static final Color SECONDARY_COLOR = new Color(71, 85, 105);     // Polished slate steel (#475569)
    public static final Color SUCCESS_COLOR = new Color(21, 128, 61);       // Workshop emerald/forest (#15803D)
    public static final Color SUCCESS_HOVER = new Color(22, 101, 52);       // Darker emerald (#166534)
    public static final Color DANGER_COLOR = new Color(220, 38, 38);        // Automotive crimson (#DC2626)
    public static final Color DANGER_HOVER = new Color(185, 28, 28);        // Deep red (#B91C1C)
    public static final Color WARNING_COLOR = new Color(217, 119, 6);       // Mechanic warm amber (#D97706)
    public static final Color INFO_COLOR = new Color(14, 116, 144);         // Steel petrol/teal (#0E7490)
    
    // Canvas & Containers
    public static final Color BG_LIGHT = new Color(243, 244, 246);          // Neutral workshop workbench tint (#F3F4F6)
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(226, 232, 240);      // Soft border gray (#E2E8F0)
    public static final Color TEXT_DARK = new Color(17, 24, 39);            // Deep charcoal black (#111827)
    public static final Color TEXT_MUTED = new Color(100, 116, 139);        // Readable slate muted (#64748B)

    // Table specific colors
    public static final Color TABLE_HEADER_BG = new Color(241, 245, 249);
    public static final Color TABLE_ROW_ALT = new Color(248, 250, 252);
    public static final Color TABLE_SELECTION_BG = new Color(224, 236, 255);

    // Typography
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_MUTED_DESC = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BIG_NUMBER = new Font("Segoe UI", Font.BOLD, 21);

    /**
     * Standard human-designed section header with title and supportive context subtitle.
     */
    public static JPanel createSectionHeader(String title, String subtitle) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(FONT_MUTED_DESC);
        lblSubtitle.setForeground(TEXT_MUTED);

        panel.add(lblTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 3)));
        panel.add(lblSubtitle);
        return panel;
    }

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(PRIMARY_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(PRIMARY_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(PRIMARY_COLOR);
            }
        });
        return btn;
    }

    public static JButton createSuccessButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(SUCCESS_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(SUCCESS_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(SUCCESS_COLOR);
            }
        });
        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(DANGER_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(DANGER_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(DANGER_COLOR);
            }
        });
        return btn;
    }

    /**
     * Clean outline button with tactile hover feedback for secondary actions.
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(51, 65, 85));
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(7, 14, 7, 14)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(new Color(241, 245, 249));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(Color.WHITE);
            }
        });
        return btn;
    }

    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));
        return card;
    }

    /**
     * Polished human-designed stat card with colored left accent indicator bar,
     * high-contrast bold metric, and descriptive subtitle note.
     */
    public static JPanel createStatCard(String title, JLabel valueLabel, Color accentColor, String subtext) {
        JPanel card = new JPanel(new BorderLayout(6, 4));
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
                new MatteBorder(0, 4, 0, 0, accentColor),
                new CompoundBorder(
                        new LineBorder(BORDER_COLOR, 1, true),
                        new EmptyBorder(10, 14, 10, 14)
                )
        ));

        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(TEXT_MUTED);

        valueLabel.setFont(FONT_BIG_NUMBER);
        valueLabel.setForeground(TEXT_DARK);

        JPanel centerPanel = new JPanel(new BorderLayout(2, 2));
        centerPanel.setOpaque(false);
        centerPanel.add(valueLabel, BorderLayout.CENTER);

        if (subtext != null && !subtext.trim().isEmpty()) {
            JLabel lblSub = new JLabel(subtext);
            lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblSub.setForeground(TEXT_MUTED);
            centerPanel.add(lblSub, BorderLayout.SOUTH);
        }

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);
        return card;
    }

    /**
     * Styles JTable with comfortable row height, subtle zebra striping, proper cell padding,
     * modern header rendering, and smart numerical/text alignment.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(32);
        table.setGridColor(new Color(235, 238, 242));
        table.setShowGrid(true);
        table.setSelectionBackground(TABLE_SELECTION_BG);
        table.setSelectionForeground(TEXT_DARK);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_SUBHEADER);
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(TEXT_DARK);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setBorder(new MatteBorder(0, 0, 1, 0, new Color(209, 213, 219)));

        // Custom cell renderer for alternating row colors, padding, and alignments
        TableCellRenderer defaultRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);

                // Alternating zebra striping
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : TABLE_ROW_ALT);
                    c.setForeground(TEXT_DARK);
                } else {
                    c.setBackground(TABLE_SELECTION_BG);
                    c.setForeground(TEXT_DARK);
                }

                // Inner cell margin padding so text doesn't touch table borders
                c.setBorder(new EmptyBorder(0, 8, 0, 8));

                // Smart column alignment
                String colName = tbl.getColumnName(column);
                if (colName != null) {
                    String lower = colName.toLowerCase();
                    if (lower.contains("amount") || lower.contains("total") || lower.contains("price")
                            || lower.contains("revenue") || lower.contains("profit") || lower.contains("cost")
                            || lower.contains("rate") || lower.contains("qty") || lower.contains("fee")
                            || lower.contains("charge") || lower.contains("balance")) {
                        c.setHorizontalAlignment(SwingConstants.RIGHT);
                    } else if (lower.equals("s.n.") || lower.equals("sn") || lower.contains("inv #")
                            || lower.contains("date") || lower.contains("status") || lower.contains("plate")
                            || lower.contains("type") || lower.contains("bills") || lower.contains("jobs")) {
                        c.setHorizontalAlignment(SwingConstants.CENTER);
                    } else {
                        c.setHorizontalAlignment(SwingConstants.LEFT);
                    }
                }
                return c;
            }
        };

        table.setDefaultRenderer(Object.class, defaultRenderer);
        table.setDefaultRenderer(Number.class, defaultRenderer);
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Workshop System", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Workshop System - Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Workshop System - Warning", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean showConfirm(Component parent, String message, String title) {
        int choice = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return choice == JOptionPane.YES_OPTION;
    }
}
