package ui;

import br.sistema.torneio.exception.RegraDeNegocioException;
import br.sistema.torneio.model.*;
import br.sistema.torneio.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

public class TelaSistemaTorneio extends JFrame {

    // Declarando Services
    private final JogadorService jogadorService = new JogadorService();
    private final TorneioService torneioService = new TorneioService();
    private final InscricaoService inscricaoService = new InscricaoService();
    private final PartidaService partidaService = new PartidaService();

    // Componentes Globais
    private JTable tabelaJogadores, tabelaTorneios, tabelaJogadoresTorneio, tabelaPartidas, tabelaRanking;
    private JTable tabelaAdminJogador, tabelaAdminTorneio;

    // Novos componentes para Jogador
    private JTable tabelaHistoricoJogador;

    // Componentes de Resumo do Torneio
    private JPanel painelBrackets;

    private JTextField campoBuscaJogador, campoBuscaTorneio;
    private JComboBox<String> comboFiltroTorneio;
    private JTabbedPane abas;

    // Fontes e Cores Modernas
    private final Font FONTE_PRINCIPAL = new Font("Segoe UI", Font.PLAIN, 14);
    private final Color COR_FUNDO = new Color(245, 247, 250);
    private final Color COR_DESTAQUE = new Color(50, 100, 200);
    private final Color COR_BRANCO = Color.WHITE;

    public TelaSistemaTorneio() {
        configurarLookFeel();
        setTitle("Sistema de Torneios E-Sports");
        setSize(1300, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(COR_FUNDO);

        criarMenu();
        criarAbas();

        setVisible(true);
    }

    private void configurarLookFeel() {
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
            UIManager.put("Button.font", FONTE_PRINCIPAL);
            UIManager.put("Label.font", FONTE_PRINCIPAL);
            UIManager.put("TextField.font", FONTE_PRINCIPAL);
            UIManager.put("Table.font", FONTE_PRINCIPAL);
            UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 14));
        } catch (Exception e) {
            System.err.println("Falha ao carregar o tema FlatLaf. Verifique a dependência.");
        }
    }

    // ======================================================
    // MENU
    // ======================================================
    private void criarMenu() {
        JMenuBar bar = new JMenuBar();
        bar.setBackground(COR_BRANCO);
        bar.setBorder(new EmptyBorder(5, 5, 5, 5));

        JMenu menu = new JMenu("Navegação");
        menu.setFont(FONTE_PRINCIPAL);

        JMenuItem inicio = new JMenuItem("Início");
        JMenuItem jogadores = new JMenuItem("Jogadores");
        JMenuItem torneios = new JMenuItem("Torneios");
        JMenuItem admin = new JMenuItem("Administrador");

        inicio.addActionListener(e -> abas.setSelectedIndex(0));
        jogadores.addActionListener(e -> abas.setSelectedIndex(1));
        torneios.addActionListener(e -> abas.setSelectedIndex(2));
        admin.addActionListener(e -> abas.setSelectedIndex(3));

        menu.add(inicio);
        menu.add(jogadores);
        menu.add(torneios);
        menu.add(admin);

        bar.add(menu);
        setJMenuBar(bar);
    }

    // ======================================================
    // ABAS
    // ======================================================
    private void criarAbas() {
        abas = new JTabbedPane();
        abas.setFont(FONTE_PRINCIPAL);
        abas.setBackground(COR_BRANCO);

        abas.add("Início", criarPainelInicio());
        abas.add("Jogadores", criarPainelJogadores());
        abas.add("Torneios", criarPainelTorneios());
        abas.add("Administrador", criarPainelAdmin());

        add(abas);
    }

    // ======================================================
    // UI HELPERS (ESTILIZAÇÃO)
    // ======================================================
    private void estilizarTabela(JTable tabela) {
        tabela.setRowHeight(30);
        tabela.setShowVerticalLines(false);
        tabela.setGridColor(new Color(230, 230, 230));
        tabela.setSelectionBackground(new Color(220, 235, 255));
        tabela.setSelectionForeground(Color.BLACK);

        JTableHeader header = tabela.getTableHeader();
        header.setBackground(COR_BRANCO);
        header.setForeground(Color.BLACK);
        ((DefaultTableCellRenderer)tabela.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

        tabela.setAutoCreateRowSorter(true);

        // Renderizador que força tudo para a esquerda, mas não quebra o tipo do dado (Integer, LocalDate)
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer() {
            @Override
            public void setValue(Object value) {
                // Se for data, formata bonitinho, senão converte normal pra texto
                if (value instanceof LocalDate) {
                    setText(((LocalDate) value).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                } else {
                    super.setValue(value);
                }
            }
        };
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);

        // Aplica o renderizador para garantir o visual à esquerda
        tabela.setDefaultRenderer(Object.class, leftRenderer);
        tabela.setDefaultRenderer(Integer.class, leftRenderer);
        tabela.setDefaultRenderer(LocalDate.class, leftRenderer);
    }

    private JButton criarBotao(String texto, Color corFundo) {
        JButton btn = new JButton(texto);
        btn.setBackground(corFundo);
        btn.setForeground(corFundo == COR_BRANCO ? Color.BLACK : Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(8, 15, 8, 15)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private DefaultTableModel criarModeloJogador() {
        return new DefaultTableModel(new Object[]{"ID", "Nome", "Nickname"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class; // ID sempre numérico para ordenar 1, 2, 10
                return String.class;
            }
        };
    }

    private DefaultTableModel criarModeloTorneio() {
        return new DefaultTableModel(new Object[]{"ID", "Nome", "Data Início", "Data Término"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class;
                // Força o Java a reconhecer essas colunas como datas, permitindo ordenar cronologicamente
                if (columnIndex == 2 || columnIndex == 3) return LocalDate.class;
                return String.class;
            }
        };
    }

    // ======================================================
    // INÍCIO
    // ======================================================
    private JPanel painelInicio;

    private JPanel criarPainelInicio() {
        painelInicio = new JPanel(new GridLayout(1, 3, 30, 30));
        painelInicio.setBorder(new EmptyBorder(40, 40, 40, 40));
        painelInicio.setBackground(COR_FUNDO);

        atualizarPainelInicio(); // Carrega os cards

        return painelInicio;
    }

    private void atualizarPainelInicio() {

        if (painelInicio == null) return;

        painelInicio.removeAll();

        // 🏆 CARD TORNEIOS
        int qtdAndamento = torneioService.torneiosEmAndamento().size();

        JPanel meioTorneios = criarPainelResumoCentral("Em andamento", String.valueOf(qtdAndamento));

        painelInicio.add(criarCardSimples(
                "🏆 Próximos Torneios",
                "Resumo dos torneios atuais",
                meioTorneios,
                "Veja todos os torneios cadastrados",
                () -> {
                    abas.setSelectedIndex(2);
                    comboFiltroTorneio.setSelectedIndex(2);
                    carregarTorneios();
                }
        ));

        // 🎮 CARD PARTIDAS
        int qtdFinalizadas = partidaService.listarTodos().size();

        JPanel meioPartidas = criarPainelResumoCentral("Partidas finalizadas", String.valueOf(qtdFinalizadas));

        painelInicio.add(criarCardSimples(
                "🎮 Partidas",
                "Resumo geral das partidas",
                meioPartidas,
                "Acompanhe o andamento dos jogos",
                () -> abas.setSelectedIndex(3)
        ));

        // ⭐ CARD JOGADORES
        List<Jogador> jogadores = jogadorService.listarTodos();
        int qtdJogadores = jogadores.size();
        String ultimo = qtdJogadores > 0
                ? jogadores.get(qtdJogadores - 1).getNickname()
                : "-";

        JPanel meioJogadores = criarPainelJogadoresResumo(qtdJogadores, ultimo);

        painelInicio.add(criarCardSimples(
                "⭐ Jogadores",
                "Estatísticas gerais",
                meioJogadores,
                "Veja todos os jogadores cadastrados",
                () -> {
                    abas.setSelectedIndex(1);
                    carregarJogadores();
                }
        ));

        painelInicio.revalidate();
        painelInicio.repaint();
    }

    private JPanel criarCardSimples(String titulo, String subtitulo, JComponent centro, String fraseRodape, Runnable acao) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(COR_BRANCO);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230,230,230), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        // TOPO
        JPanel topo = new JPanel(new GridLayout(2, 1));
        topo.setBackground(COR_BRANCO);

        JLabel lblTitulo = new JLabel(titulo, SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(COR_DESTAQUE);

        JLabel lblSub = new JLabel(subtitulo, SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(Color.GRAY);

        topo.add(lblTitulo);
        topo.add(lblSub);

        // CENTRO
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(COR_BRANCO);
        centerWrapper.add(centro);

        // RODAPÉ
        JPanel bottom = new JPanel(new BorderLayout(5, 5));
        bottom.setBackground(COR_BRANCO);

        JLabel frase = new JLabel("<html><center><i>" + fraseRodape + "</i></center></html>", SwingConstants.CENTER);
        frase.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        frase.setForeground(new Color(150,150,150));

        JButton btn = criarBotao("Acessar", COR_DESTAQUE);
        btn.addActionListener(e -> acao.run());

        bottom.add(frase, BorderLayout.NORTH);
        bottom.add(btn, BorderLayout.SOUTH);

        card.add(topo, BorderLayout.NORTH);
        card.add(centerWrapper, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    private JPanel criarPainelResumoCentral(String titulo, String valor) {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(COR_BRANCO);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTitulo.setForeground(Color.GRAY);

        JLabel lblValor = new JLabel(valor);
        lblValor.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 42));
        lblValor.setForeground(COR_DESTAQUE);

        painel.add(lblTitulo);
        painel.add(Box.createVerticalStrut(10));
        painel.add(lblValor);

        return painel;
    }

    private JPanel criarPainelJogadoresResumo(int total, String ultimo) {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(COR_BRANCO);

        JLabel lblTotal = new JLabel("Total cadastrados");
        lblTotal.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTotal.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTotal.setForeground(Color.GRAY);

        JLabel lblNumero = new JLabel(String.valueOf(total));
        lblNumero.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblNumero.setFont(new Font("Segoe UI", Font.BOLD, 42));
        lblNumero.setForeground(COR_DESTAQUE);

        JLabel lblUltimo = new JLabel("Último membro: " + ultimo);
        lblUltimo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblUltimo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUltimo.setForeground(new Color(120,120,120));

        painel.add(lblTotal);
        painel.add(Box.createVerticalStrut(5));
        painel.add(lblNumero);
        painel.add(Box.createVerticalStrut(15));
        painel.add(lblUltimo);

        return painel;
    }

    // ======================================================
    // JOGADORES
    // ======================================================
    private JPanel criarPainelJogadores() {
        JPanel p = new JPanel(new BorderLayout(20, 20));
        p.setBorder(new EmptyBorder(30, 30, 30, 30));
        p.setBackground(COR_BRANCO);

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.setBackground(COR_BRANCO);

        campoBuscaJogador = new JTextField(25);
        JButton btnBuscar = criarBotao("Buscar", new Color(50, 137, 220));
        btnBuscar.addActionListener(e -> carregarJogadores());

        topo.add(new JLabel("Pesquisar Jogador: "));
        topo.add(campoBuscaJogador);
        topo.add(btnBuscar);

        tabelaJogadores = new JTable();
        estilizarTabela(tabelaJogadores);
        tabelaJogadores.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) mostrarPerfilJogador();
        });

        p.add(topo, BorderLayout.NORTH);
        p.add(new JScrollPane(tabelaJogadores), BorderLayout.CENTER);

        // ÁREA INFERIOR DE JOGADORES
        JTabbedPane detalhesJogador = new JTabbedPane();
        tabelaHistoricoJogador = new JTable();
        estilizarTabela(tabelaHistoricoJogador);

        detalhesJogador.add("Torneios Disputados", new JScrollPane(tabelaHistoricoJogador));
        detalhesJogador.setPreferredSize(new Dimension(0, 250));

        p.add(detalhesJogador, BorderLayout.SOUTH);

        carregarJogadores();
        return p;
    }

    private void carregarJogadores() {
        DefaultTableModel model = criarModeloJogador();

        try {
            List<Jogador> lista = campoBuscaJogador != null && !campoBuscaJogador.getText().isEmpty()
                    ? jogadorService.buscarNomeJogador(campoBuscaJogador.getText())
                    : jogadorService.listarTodos();

            for (Jogador j : lista) {
                model.addRow(new Object[]{j.getId(), j.getNome(), j.getNickname()});
            }

            tabelaJogadores.setModel(model);

        } catch (RegraDeNegocioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Atenção", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void mostrarPerfilJogador() {
        if (tabelaJogadores.getSelectedRow() == -1) return;

        int viewRow = tabelaJogadores.getSelectedRow();
        int modelRow = tabelaJogadores.convertRowIndexToModel(viewRow);
        int idJogador = (int) tabelaJogadores.getModel().getValueAt(modelRow, 0);

        DefaultTableModel model = new DefaultTableModel(new Object[]{"ID Torneio", "Nome do Torneio", "Colocação"}, 0);

        try {
            // Pega a lista direto do banco
            List<Object[]> historico = inscricaoService.listarHistoricoPorJogador(idJogador);
            for (Object[] linha : historico) {
                model.addRow(linha);
            }
        } catch (RegraDeNegocioException e) {
            System.err.println("Erro ao carregar histórico: " + e.getMessage());
        }

        tabelaHistoricoJogador.setModel(model);
    }

    // ======================================================
    // TORNEIOS
    // ======================================================
    private JPanel criarPainelTorneios() {
        JPanel p = new JPanel(new BorderLayout(20, 20));
        p.setBorder(new EmptyBorder(30, 30, 30, 30));
        p.setBackground(COR_BRANCO);

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.setBackground(COR_BRANCO);

        campoBuscaTorneio = new JTextField(20);
        comboFiltroTorneio = new JComboBox<>(new String[]{"Todos", "Futuros", "Em andamento", "Finalizados", "Cancelados"});
        JButton btnBuscar = criarBotao("Buscar", new Color(50, 137, 220));
//        JButton btnBrackets = criarBotao("Ver Chaves (Brackets)", new Color(46, 204, 113));

        btnBuscar.addActionListener(e -> carregarTorneios());
//        btnBrackets.addActionListener(e -> mostrarBrackets());

        topo.add(new JLabel("Nome:"));
        topo.add(campoBuscaTorneio);
        topo.add(new JLabel("Status:"));
        topo.add(comboFiltroTorneio);
        topo.add(btnBuscar);
//        topo.add(Box.createHorizontalStrut(20));
//        topo.add(btnBrackets);

        tabelaTorneios = new JTable();
        estilizarTabela(tabelaTorneios);
        tabelaTorneios.getSelectionModel().addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting()) carregarDetalhesTorneio();
        });

        p.add(topo, BorderLayout.NORTH);
        p.add(new JScrollPane(tabelaTorneios), BorderLayout.CENTER);

        // Painel inferior com abas de detalhes
        JTabbedPane detalhes = new JTabbedPane();
        tabelaJogadoresTorneio = new JTable(); estilizarTabela(tabelaJogadoresTorneio);
        tabelaPartidas = new JTable(); estilizarTabela(tabelaPartidas);
        tabelaRanking = new JTable(); estilizarTabela(tabelaRanking);

        detalhes.add("Inscritos", new JScrollPane(tabelaJogadoresTorneio));
        detalhes.add("Partidas", new JScrollPane(tabelaPartidas));
        detalhes.add("Ranking", new JScrollPane(tabelaRanking));

        // Aba Brackets (Chaves)
        painelBrackets = new JPanel(new GridLayout(1, 4, 10, 10)); // Container para as chaves
        painelBrackets.setBackground(new Color(50, 50, 50));
        painelBrackets.setBorder(new EmptyBorder(10,10,10,10));
        detalhes.add("Chaves (Brackets)", new JScrollPane(painelBrackets));

        detalhes.setPreferredSize(new Dimension(0, 300));
        p.add(detalhes, BorderLayout.SOUTH);

        carregarTorneios();
        return p;
    }

    private void carregarTorneios() {

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "Nome", "Data Início", "Data Término"}, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (getRowCount() > 0 && getValueAt(0, columnIndex) != null) {
                    return getValueAt(0, columnIndex).getClass();
                }
                return Object.class;
            }
        };

        try {
            String busca = campoBuscaTorneio != null
                    ? campoBuscaTorneio.getText().trim()
                    : "";

            int filtro = comboFiltroTorneio.getSelectedIndex();
            List<Torneio> lista;

            // SE HOUVER BUSCA → chama buscarNomeTorneio()
            if (!busca.isEmpty()) {
                lista = torneioService.buscarNomeTorneio(busca);

                // Se houver filtro além de "Todos", aplica o filtro sobre o resultado da busca
                if (filtro != 0) {
                    lista = aplicarFiltroStatus(lista, filtro);
                }

            } else {
                // SEM BUSCA → usa apenas filtro
                if (filtro == 1) lista = torneioService.torneiosNaoIniciados();
                else if (filtro == 2) lista = torneioService.torneiosEmAndamento();
                else if (filtro == 3) lista = torneioService.torneiosFinalizados();
                else if (filtro == 4) lista = torneioService.torneiosCancelados();
                else lista = torneioService.listarTodos();
            }

            for (Torneio t : lista) {
                model.addRow(new Object[]{
                        t.getId(),
                        t.getNome(),
                        t.getDataInicio(),
                        t.getDataTermino()
                });
            }

            tabelaTorneios.setModel(model);

            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);

            sorter.setComparator(3, (o1, o2) -> {
                LocalDate d1 = (LocalDate) o1;
                LocalDate d2 = (LocalDate) o2;

                if (d1 == null && d2 == null) return 0;
                if (d1 == null) return 1;
                if (d2 == null) return -1;

                return d1.compareTo(d2);
            });

            sorter.setSortsOnUpdates(true);
            tabelaTorneios.setRowSorter(sorter);

            atualizarPainelInicio();

        } catch (RegraDeNegocioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private List<Torneio> aplicarFiltroStatus(List<Torneio> lista, int filtro) {

        LocalDate hoje = LocalDate.now();

        return switch (filtro) {

            case 1 -> lista.stream()
                    .filter(t -> t.getDataInicio() != null && t.getDataInicio().isAfter(hoje))
                    .collect(Collectors.toList());

            case 2 -> lista.stream()
                    .filter(t -> t.getDataInicio() != null
                            && !t.getDataInicio().isAfter(hoje)
                            && (t.getDataTermino() == null || t.getDataTermino().isAfter(hoje)))
                    .collect(Collectors.toList());

            case 3 -> lista.stream()
                    .filter(t -> t.getDataTermino() != null
                            && !t.getDataTermino().isAfter(hoje))
                    .collect(Collectors.toList());

            case 4 -> lista.stream()
                    .filter(t -> t.getDataInicio() == null && t.getDataTermino() == null)
                    .collect(Collectors.toList());

            default -> lista;
        };
    }

    private void carregarDetalhesTorneio() {

        if (tabelaTorneios.getSelectedRow() == -1) return;

        int id = (int) tabelaTorneios.getValueAt(tabelaTorneios.getSelectedRow(), 0);

        try {
            // INSCRITOS
            int qtdInscritos = inscricaoService.contarInscritos(id);
            DefaultTableModel m1 = new DefaultTableModel(new Object[]{"ID", "Nome", "Nick"}, 0);

            for (Jogador j : inscricaoService.listarPorTorneio(id)) {
                m1.addRow(new Object[]{
                        j.getId(),
                        j.getNome(),
                        j.getNickname()
                });
            }

            tabelaJogadoresTorneio.setModel(m1);

            JTabbedPane abasDetalhes = (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, tabelaJogadoresTorneio);
            Container scrollInscritos = SwingUtilities.getAncestorOfClass(JScrollPane.class, tabelaJogadoresTorneio);

            if (abasDetalhes != null && scrollInscritos != null) {
                int indexAba = abasDetalhes.indexOfComponent(scrollInscritos);
                if (indexAba != -1) {
                    abasDetalhes.setTitleAt(indexAba, "Inscritos (" + qtdInscritos + ")");
                }
            }

            // PARTIDAS
            DefaultTableModel m2 = new DefaultTableModel(new Object[]{"Fase", "Jogador 1", "Placar", "Jogador 2"}, 0);

            List<PartidaDTO> partidas = partidaService.listarPorTorneio(id);

            for (PartidaDTO p : partidas) {
                m2.addRow(new Object[]{
                        p.getNomeFase(),
                        p.getNickJogador1(),
                        (p.getPlacar1() == 0 && p.getPlacar2() == 0)
                                ? "-"
                                : p.getPlacar1() + " x " + p.getPlacar2(),
                        p.getNickJogador2()
                });
            }

            tabelaPartidas.setModel(m2);
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(m2);

            sorter.setComparator(0, (o1, o2) -> {
                String f1 = o1.toString();
                String f2 = o2.toString();

                return ordemFaseUI(f1) - ordemFaseUI(f2);
            });

            tabelaPartidas.setRowSorter(sorter);

            // RANKING
            DefaultTableModel m3 = new DefaultTableModel(new Object[]{"Colocação", "Nickname"}, 0);

            List<Inscricao> ranking = inscricaoService.listarRanking(id);

            for (Inscricao i : ranking) {
                Jogador j = jogadorService.buscarPorId(i.getIdJogador());

                String nick = (j != null) ? j.getNickname() : "Desconhecido";

                String colFormatada;

                switch (i.getColocacaoFinal()) {
                    case 1 -> colFormatada = "1º Lugar";
                    case 2 -> colFormatada = "2º Lugar";
                    case 3 -> colFormatada = "3-4º";
                    case 5 -> colFormatada = "5-8º";
                    default -> colFormatada = i.getColocacaoFinal() + "º";
                }

                m3.addRow(new Object[]{colFormatada, nick});
            }

            tabelaRanking.setModel(m3);

        } catch (RegraDeNegocioException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Erro ao carregar detalhes: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private int ordemFaseUI(String nome) {

        if (nome == null) return 99;

        if (nome.equalsIgnoreCase("Quartas de Final")) return 1;
        if (nome.equalsIgnoreCase("Semifinal")) return 2;
        if (nome.equalsIgnoreCase("Final")) return 3;

        return 99;
    }

    private void mostrarBrackets() {
        if (tabelaTorneios.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um torneio primeiro.");
            return;
        }
        int idTorneio = (int) tabelaTorneios.getValueAt(tabelaTorneios.getSelectedRow(), 0);

        JDialog dialog = new JDialog(this, "Chaves do Torneio", true);
        dialog.setSize(1000, 600);
        dialog.setLocationRelativeTo(this);

        JPanel container = new JPanel(new GridLayout(1, 4, 10, 10)); // 4 Colunas para as fases
        container.setBorder(new EmptyBorder(20,20,20,20));
        container.setBackground(new Color(50, 50, 50)); // Fundo escuro para bracket

        // Pegar partidas e agrupar por fase
        try {
            List<PartidaDTO> partidas = partidaService.listarPorTorneio(idTorneio);

            // Criar colunas
            container.add(criarColunaBracket("Oitavas", partidas));
            container.add(criarColunaBracket("Quartas", partidas));
            container.add(criarColunaBracket("Semifinal", partidas));
            container.add(criarColunaBracket("Final", partidas));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            return;
        }

        dialog.add(new JScrollPane(container));
        dialog.setVisible(true);
    }

    private JPanel criarColunaBracket(String nomeFase, List<PartidaDTO> todasPartidas) {
        JPanel coluna = new JPanel();
        coluna.setLayout(new BoxLayout(coluna, BoxLayout.Y_AXIS));
        coluna.setBackground(new Color(60, 63, 65));
        coluna.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel titulo = new JLabel(nomeFase);
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        coluna.add(titulo);
        coluna.add(Box.createVerticalStrut(20));

        List<PartidaDTO> filtradas = todasPartidas.stream()
                .filter(p -> p.getNomeFase() != null && p.getNomeFase().equalsIgnoreCase(nomeFase))
                .collect(Collectors.toList());

        if (filtradas.isEmpty()) {
            JLabel vazio = new JLabel("-");
            vazio.setForeground(Color.GRAY);
            vazio.setAlignmentX(Component.CENTER_ALIGNMENT);
            coluna.add(vazio);
        } else {
            for (PartidaDTO p : filtradas) {
                JPanel card = new JPanel(new GridLayout(2, 1));
                card.setBackground(Color.WHITE);
                card.setBorder(new LineBorder(Color.BLACK, 1));
                card.setMaximumSize(new Dimension(200, 60));

                JLabel j1 = new JLabel(" " + p.getNickJogador1() + " (" + p.getPlacar1() + ")");
                JLabel j2 = new JLabel(" " + p.getNickJogador2() + " (" + p.getPlacar2() + ")");

                // Destacar vencedor (simples lógica visual)
                if(p.getPlacar1() > p.getPlacar2()) j1.setFont(new Font("Segoe UI", Font.BOLD, 12));
                if(p.getPlacar2() > p.getPlacar1()) j2.setFont(new Font("Segoe UI", Font.BOLD, 12));

                card.add(j1);
                card.add(j2);

                coluna.add(card);
                coluna.add(Box.createVerticalStrut(20)); // Espaço entre partidas
            }
        }
        return coluna;
    }

    // ======================================================
    // ADMIN (LÓGICA REFEITA)
    // ======================================================
    private JPanel criarPainelAdmin() {
        JTabbedPane admin = new JTabbedPane();
        admin.setFont(FONTE_PRINCIPAL);

        admin.add("Gerenciar Jogadores", criarAdminJogador());
        admin.add("Gerenciar Torneios", criarAdminTorneio());
        admin.add("Inscrições", criarAdminInscricao());

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(COR_BRANCO);
        p.setBorder(new EmptyBorder(20,20,20,20));
        p.add(admin, BorderLayout.CENTER);
        return p;
    }

    // ADMIN JOGADOR
    private JPanel painelFormJogador;
    private JTextField txtNomeJog, txtNickJog, txtIdJog;

    private JPanel criarAdminJogador() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(COR_BRANCO);

        tabelaAdminJogador = new JTable();
        estilizarTabela(tabelaAdminJogador);
        p.add(new JScrollPane(tabelaAdminJogador), BorderLayout.CENTER);

        tabelaAdminJogador.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabelaAdminJogador.getSelectedRow() != -1) {
                int row = tabelaAdminJogador.convertRowIndexToModel(tabelaAdminJogador.getSelectedRow());
                txtIdJog.setText(tabelaAdminJogador.getModel().getValueAt(row, 0).toString());
                txtNomeJog.setText(tabelaAdminJogador.getModel().getValueAt(row, 1).toString());
                txtNickJog.setText(tabelaAdminJogador.getModel().getValueAt(row, 2).toString());
            }
        });

        // Painel de Controle
        JPanel controle = new JPanel(new BorderLayout());
        controle.setBackground(COR_BRANCO);
        controle.setBorder(new TitledBorder("Ações"));

        // Botões de Modo
        JPanel botoesModo = new JPanel(new FlowLayout());
        botoesModo.setBackground(COR_BRANCO);
        JButton btnInserir = criarBotao("Inserir", COR_DESTAQUE);
        JButton btnModificar = criarBotao("Modificar", Color.ORANGE);
        JButton btnRemover = criarBotao("Remover", Color.RED);

        botoesModo.add(btnInserir);
        botoesModo.add(btnModificar);
        botoesModo.add(btnRemover);

        // Formulário Dinâmico
        painelFormJogador = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelFormJogador.setBackground(COR_FUNDO);
        painelFormJogador.setBorder(new EmptyBorder(10,10,10,10));

        txtNomeJog = new JTextField(15);
        txtNickJog = new JTextField(10);
        txtIdJog = new JTextField(5);

        // Ações dos botões de modo
        btnInserir.addActionListener(e -> configurarFormJogador("INSERIR"));
        btnModificar.addActionListener(e -> configurarFormJogador("MODIFICAR"));
        btnRemover.addActionListener(e -> configurarFormJogador("REMOVER"));

        controle.add(botoesModo, BorderLayout.NORTH);
        controle.add(painelFormJogador, BorderLayout.CENTER);

        p.add(controle, BorderLayout.SOUTH);
        carregarJogadoresAdmin();

        // Estado inicial vazio
        painelFormJogador.add(new JLabel("Selecione uma ação acima."));

        return p;
    }

    private void carregarJogadoresAdmin() {
        DefaultTableModel model = criarModeloJogador();
        for (Jogador j : jogadorService.listarTodos()) {
            model.addRow(new Object[]{j.getId(), j.getNome(), j.getNickname()});
        }
        tabelaAdminJogador.setModel(model);
    }

    private void configurarFormJogador(String modo) {
        painelFormJogador.removeAll();
        JButton btnConfirmar = criarBotao("Confirmar", modo.equals("INSERIR") ? COR_DESTAQUE : (modo.equals("MODIFICAR") ? Color.ORANGE : Color.RED));

        if (modo.equals("INSERIR") || modo.equals("MODIFICAR")) {
            if (modo.equals("MODIFICAR")) {
                painelFormJogador.add(new JLabel("ID:")); painelFormJogador.add(txtIdJog);
            }
            painelFormJogador.add(new JLabel("Nome:")); painelFormJogador.add(txtNomeJog);
            painelFormJogador.add(new JLabel("Nick:")); painelFormJogador.add(txtNickJog);

            btnConfirmar.addActionListener(e -> {
                try {
                    String nome = txtNomeJog.getText().trim();
                    String nick = txtNickJog.getText().trim();

                    // Dispara o erro se as caixas estiverem vazias
                    if (nome.isEmpty() || nick.isEmpty()) {
                        throw new RegraDeNegocioException("Caixa vazia! Nome e Nickname são obrigatórios.");
                    }

                    Jogador j = new Jogador();
                    j.setNome(nome);
                    j.setNickname(nick);

                    if (modo.equals("INSERIR")) {
                        jogadorService.inserir(j);
                    } else {
                        jogadorService.atualizar(txtIdJog.getText(), j);
                    }

                    JOptionPane.showMessageDialog(this, "Operação realizada com sucesso!");
                    carregarJogadoresAdmin();
                    carregarJogadores(); // Recarrega
                    atualizarPainelInicio();
                    txtNomeJog.setText(""); txtNickJog.setText(""); txtIdJog.setText("");
                } catch (RegraDeNegocioException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Atenção", JOptionPane.WARNING_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao salvar. Nickname já está em uso.", "Erro no Banco", JOptionPane.ERROR_MESSAGE);
                }
            });
        } else if (modo.equals("REMOVER")) {
            painelFormJogador.add(new JLabel("ID para remover:")); painelFormJogador.add(txtIdJog);
            btnConfirmar.addActionListener(e -> {
                try {
                    jogadorService.deletar(txtIdJog.getText());
                    JOptionPane.showMessageDialog(this, "Jogador removido!");
                    carregarJogadoresAdmin();
                    carregarJogadores();
                    atualizarPainelInicio();
                    txtIdJog.setText("");
                } catch (RegraDeNegocioException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erro inesperado: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            });
        }

        painelFormJogador.add(btnConfirmar);
        painelFormJogador.revalidate();
        painelFormJogador.repaint();
    }

    // ADMIN TORNEIO
    private JPanel painelFormTorneio;
    private JTextField txtNomeTorneio, txtDataTorneio, txtDataTerminoTorneio, txtIdTorneio;

    private JPanel criarAdminTorneio() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(COR_BRANCO);
        tabelaAdminTorneio = new JTable();
        estilizarTabela(tabelaAdminTorneio);
        p.add(new JScrollPane(tabelaAdminTorneio), BorderLayout.CENTER);

        // Preencher automático ao clicar
        tabelaAdminTorneio.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabelaAdminTorneio.getSelectedRow() != -1) {
                int row = tabelaAdminTorneio.convertRowIndexToModel(tabelaAdminTorneio.getSelectedRow());
                txtIdTorneio.setText(tabelaAdminTorneio.getModel().getValueAt(row, 0).toString());
                txtNomeTorneio.setText(tabelaAdminTorneio.getModel().getValueAt(row, 1).toString());

                Object dtIni = tabelaAdminTorneio.getModel().getValueAt(row, 2);
                txtDataTorneio.setText(dtIni != null ? dtIni.toString() : "");

                Object dtFim = tabelaAdminTorneio.getModel().getValueAt(row, 3);
                txtDataTerminoTorneio.setText(dtFim != null ? dtFim.toString() : "");
            }
        });

        JPanel controle = new JPanel(new BorderLayout());
        controle.setBackground(COR_BRANCO);
        controle.setBorder(new TitledBorder("Ações"));

        JPanel botoes = new JPanel(new FlowLayout());
        botoes.setBackground(COR_BRANCO);
        JButton btnInsert = criarBotao("Inserir", COR_DESTAQUE);
        JButton btnUpd = criarBotao("Modificar", Color.ORANGE);
        JButton btnDel = criarBotao("Remover", Color.RED);

        JButton btnPartidas = criarBotao("Gerenciar Partidas", new Color(46, 204, 113));

        botoes.add(btnInsert); botoes.add(btnUpd); botoes.add(btnDel); botoes.add(btnPartidas);

        painelFormTorneio = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelFormTorneio.setBackground(COR_FUNDO);
        painelFormTorneio.setBorder(new EmptyBorder(10,10,10,10));

        txtNomeTorneio = new JTextField(15);
        txtDataTorneio = new JTextField(10);
        txtDataTerminoTorneio = new JTextField(10); // NOVO CAMPO
        txtIdTorneio = new JTextField(5);

        btnInsert.addActionListener(e -> configurarFormTorneio("INSERIR"));
        btnUpd.addActionListener(e -> configurarFormTorneio("MODIFICAR"));
        btnDel.addActionListener(e -> configurarFormTorneio("REMOVER"));

        btnPartidas.addActionListener(e -> abrirJanelaPartidasAdmin());

        controle.add(botoes, BorderLayout.NORTH);
        controle.add(painelFormTorneio, BorderLayout.CENTER);

        p.add(controle, BorderLayout.SOUTH);
        carregarTorneiosAdmin();

        painelFormTorneio.add(new JLabel("Selecione uma ação acima."));
        return p;
    }

    private void carregarTorneiosAdmin() {
        DefaultTableModel model = criarModeloTorneio(); // CORREÇÃO AQUI
        for (Torneio t : torneioService.listarTodos()) {
            model.addRow(new Object[]{t.getId(), t.getNome(), t.getDataInicio(), t.getDataTermino()});
        }
        tabelaAdminTorneio.setModel(model);
    }

    private void configurarFormTorneio(String modo) {
        painelFormTorneio.removeAll();
        Color corBotao = modo.equals("INSERIR") ? COR_DESTAQUE : (modo.equals("MODIFICAR") ? Color.ORANGE : Color.RED);
        JButton btn = criarBotao("Confirmar", corBotao);

        if (modo.equals("INSERIR") || modo.equals("MODIFICAR")) {
            if (modo.equals("MODIFICAR")) {
                painelFormTorneio.add(new JLabel("ID:")); painelFormTorneio.add(txtIdTorneio);
            }
            painelFormTorneio.add(new JLabel("Nome:")); painelFormTorneio.add(txtNomeTorneio);
            painelFormTorneio.add(new JLabel("Início (AAAA-MM-DD):")); painelFormTorneio.add(txtDataTorneio);
            painelFormTorneio.add(new JLabel("Término:")); painelFormTorneio.add(txtDataTerminoTorneio);

            btn.addActionListener(e -> {
                try {
                    Torneio t = new Torneio();
                    if(!txtNomeTorneio.getText().trim().isEmpty()) t.setNome(txtNomeTorneio.getText());
                    if(!txtDataTorneio.getText().trim().isEmpty()) t.setDataInicio(LocalDate.parse(txtDataTorneio.getText().trim()));
                    if(!txtDataTerminoTorneio.getText().trim().isEmpty()) t.setDataTermino(LocalDate.parse(txtDataTerminoTorneio.getText().trim()));

                    if (modo.equals("INSERIR")) {
                        torneioService.inserir(t);
                    } else {
                        // Passa a STRING do ID
                        torneioService.atualizar(txtIdTorneio.getText(), t);
                    }

                    JOptionPane.showMessageDialog(this, "Operação realizada com sucesso!");
                    carregarTorneiosAdmin();
                    carregarTorneios();
                    atualizarPainelInicio();
                    txtNomeTorneio.setText(""); txtDataTorneio.setText(""); txtDataTerminoTorneio.setText(""); txtIdTorneio.setText("");
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this, "Data inválida. Use o formato AAAA-MM-DD", "Atenção", JOptionPane.WARNING_MESSAGE);
                } catch (RegraDeNegocioException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Atenção", JOptionPane.WARNING_MESSAGE);
                }
            });
        } else if (modo.equals("REMOVER")) {
            painelFormTorneio.add(new JLabel("ID:")); painelFormTorneio.add(txtIdTorneio);
            btn.addActionListener(e -> {
                try {
                    // Passa a STRING do ID
                    torneioService.deletar(txtIdTorneio.getText());
                    JOptionPane.showMessageDialog(this, "Torneio removido!");
                    carregarTorneiosAdmin();
                    carregarTorneios();
                    atualizarPainelInicio();
                    txtIdTorneio.setText("");
                } catch (RegraDeNegocioException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            });
        }

        painelFormTorneio.add(btn);
        painelFormTorneio.revalidate();
        painelFormTorneio.repaint();
    }

    private void abrirJanelaPartidasAdmin() {
        // 1. Verifica se tem torneio selecionado
        if (tabelaAdminTorneio.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um torneio na tabela primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Pega o ID e o Nome do Torneio
        int row = tabelaAdminTorneio.convertRowIndexToModel(tabelaAdminTorneio.getSelectedRow());
        int idTorneio = Integer.parseInt(tabelaAdminTorneio.getModel().getValueAt(row, 0).toString());
        String nomeTorneio = tabelaAdminTorneio.getModel().getValueAt(row, 1).toString();

        // 3. Cria a Janela Modal
        JDialog dialog = new JDialog(this, "Gerenciar Partidas - " + nomeTorneio, true);
        dialog.setSize(800, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        // 4. Cria a Tabela de Partidas para esta janela
        JTable tableJanelaPartidas = new JTable();
        estilizarTabela(tableJanelaPartidas);
        dialog.add(new JScrollPane(tableJanelaPartidas), BorderLayout.CENTER);

        // 5. Painel inferior para digitar o placar
        JPanel painelPlacar = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        painelPlacar.setBackground(COR_BRANCO);
        painelPlacar.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lblJ1 = new JLabel("Jogador 1:");
        JTextField txtPlacar1 = new JTextField(5);
        JLabel lblX = new JLabel(" X ");
        JTextField txtPlacar2 = new JTextField(5);
        JLabel lblJ2 = new JLabel(":Jogador 2");
        JButton btnSalvarPlacar = criarBotao("Salvar Resultado", COR_DESTAQUE);

        painelPlacar.add(lblJ1); painelPlacar.add(txtPlacar1);
        painelPlacar.add(lblX);
        painelPlacar.add(txtPlacar2); painelPlacar.add(lblJ2);
        painelPlacar.add(btnSalvarPlacar);

        dialog.add(painelPlacar, BorderLayout.SOUTH);

        // 6. Função para carregar a tabela
        Runnable atualizarTabelaJanela = () -> {
            DefaultTableModel m = new DefaultTableModel(new Object[]{"ID Partida", "Fase", "Jogador 1", "Placar 1", "Placar 2", "Jogador 2"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            try {
                for (PartidaDTO p : partidaService.listarPorTorneio(idTorneio)) {
                    m.addRow(new Object[]{p.getIdPartida(), p.getNomeFase(), p.getNickJogador1(), p.getPlacar1(), p.getPlacar2(), p.getNickJogador2()});
                }
                tableJanelaPartidas.setModel(m);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erro ao carregar partidas: " + ex.getMessage());
            }
        };

        atualizarTabelaJanela.run(); // Carrega na abertura

        // 7. Evento ao clicar na tabela (preenche os nomes dos jogadores nos labels)
        tableJanelaPartidas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableJanelaPartidas.getSelectedRow() != -1) {
                int r = tableJanelaPartidas.convertRowIndexToModel(tableJanelaPartidas.getSelectedRow());
                lblJ1.setText(tableJanelaPartidas.getModel().getValueAt(r, 2).toString());
                lblJ2.setText(tableJanelaPartidas.getModel().getValueAt(r, 5).toString());

                Object p1 = tableJanelaPartidas.getModel().getValueAt(r, 3);
                Object p2 = tableJanelaPartidas.getModel().getValueAt(r, 4);
                txtPlacar1.setText(p1 != null ? p1.toString() : "");
                txtPlacar2.setText(p2 != null ? p2.toString() : "");
            }
        });

        // 8. Evento de Salvar o Placar
        btnSalvarPlacar.addActionListener(e -> {

            if (tableJanelaPartidas.getSelectedRow() == -1) {
                JOptionPane.showMessageDialog(dialog, "Selecione uma partida.");
                return;
            }

            try {
                int r = tableJanelaPartidas.convertRowIndexToModel(tableJanelaPartidas.getSelectedRow());

                int idPartida = (int) tableJanelaPartidas.getModel().getValueAt(r, 0);

                int p1 = Integer.parseInt(txtPlacar1.getText().trim());
                int p2 = Integer.parseInt(txtPlacar2.getText().trim());

                partidaService.registrarResultado(idPartida, p1, p2);

                JOptionPane.showMessageDialog(dialog, "Resultado registrado!");

                atualizarTabelaJanela.run();
                carregarDetalhesTorneio();
                atualizarPainelInicio();
                carregarTorneiosAdmin();
                carregarTorneios();

                txtPlacar1.setText("");
                txtPlacar2.setText("");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage());
            }
        });

        dialog.setVisible(true); // Exibe a janela
    }

    // ADMIN INSCRIÇÃO
    private JPanel criarAdminInscricao() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(COR_BRANCO);

        // FORMULÁRIO 1: INSCREVER
        JPanel formAdicionar = new JPanel(new GridLayout(3, 2, 10, 10));
        formAdicionar.setBorder(BorderFactory.createTitledBorder("Inscrever Jogador em Torneio"));
        formAdicionar.setBackground(COR_BRANCO);

        JTextField txtIdTAdd = new JTextField(5);
        JTextField txtIdJAdd = new JTextField(5);
        JButton btnInscrever = criarBotao("Realizar Inscrição", COR_DESTAQUE);

        formAdicionar.add(new JLabel(" ID Torneio:"));
        formAdicionar.add(txtIdTAdd);
        formAdicionar.add(new JLabel(" ID Jogador:"));
        formAdicionar.add(txtIdJAdd);
        formAdicionar.add(new JLabel("")); // Espaço vazio para alinhar o botão
        formAdicionar.add(btnInscrever);

        btnInscrever.addActionListener(e -> {
            try {
                inscricaoService.inserir(txtIdTAdd.getText(), txtIdJAdd.getText());
                JOptionPane.showMessageDialog(this, "Inscrição realizada com sucesso!");
                txtIdTAdd.setText("");
                txtIdJAdd.setText("");
            } catch (RegraDeNegocioException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro crítico: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        // FORMULÁRIO 2: REMOVER
        JPanel formRemover = new JPanel(new GridLayout(3, 2, 10, 10));
        formRemover.setBorder(BorderFactory.createTitledBorder("Remover Jogador do Torneio"));
        formRemover.setBackground(COR_BRANCO);

        JTextField txtIdTRem = new JTextField(5);
        JTextField txtIdJRem = new JTextField(5);
        JButton btnRemover = criarBotao("Remover Inscrição", Color.RED);

        formRemover.add(new JLabel(" ID Torneio:"));
        formRemover.add(txtIdTRem);
        formRemover.add(new JLabel(" ID Jogador:"));
        formRemover.add(txtIdJRem);
        formRemover.add(new JLabel("")); // Espaço vazio para alinhar o botão
        formRemover.add(btnRemover);

        btnRemover.addActionListener(e -> {
            try {
                inscricaoService.removerInscricao(txtIdTRem.getText(), txtIdJRem.getText());
                JOptionPane.showMessageDialog(this, "Inscrição removida com sucesso!");
                txtIdTRem.setText("");
                txtIdJRem.setText("");
            } catch (RegraDeNegocioException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro crítico: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        // POSICIONAMENTO NA TELA (GRID)
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0; // Ambos ficarão na mesma linha horizontal
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 20, 0, 20); // Dá um respiro/margem entre os dois formulários

        // Posição do Form de Adicionar
        gbc.gridx = 0;
        p.add(formAdicionar, gbc);

        // Posição do Form de Remover
        gbc.gridx = 1;
        p.add(formRemover, gbc);

        return p;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TelaSistemaTorneio::new);
    }
}